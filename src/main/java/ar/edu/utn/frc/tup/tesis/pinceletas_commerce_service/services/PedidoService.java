package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.Rabbit.NotificacionEventService;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.configs.UserAuthClient;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.*;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.*;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.AccionAuditoria;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.EstadoPedido;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.*;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.exceptions.DireccionIncompletaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final ProductoRepository productoRepository;
    private final OpcionProductoRepository opcionProductoRepository;
    private final CarritoRepository carritoRepository;
    private final MercadoPagoService mercadoPagoService;
    private final UserAuthClient userAuthClient;
    private final ModelMapper modelMapper;
    private final NotificacionEventService notificacionEventService;

    // 🔔 NUEVO: Repositorio de auditoría
    private final AuditoriaPedidoRepository auditoriaPedidoRepository;

    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoRequest, String authToken) {
        log.info("Iniciando creación de pedido");

        UserResponseDTO usuario = userAuthClient.obtenerUsuarioPorEmail(
                obtenerEmailUsuarioDesdeRequest(pedidoRequest),
                authToken
        );
        log.info("Usuario obtenido: {}", usuario.getEmail());

        validarDireccionUsuario(usuario);

        BigDecimal total = calcularTotalPedido(pedidoRequest.getItems());
        log.info("Total del pedido calculado: {}", total);

        PedidoEntity pedido = new PedidoEntity();
        pedido.setNumeroPedido(generarNumeroPedido());
        pedido.setUsuarioId(usuario.getId());
        pedido.setTotal(total);
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());

        pedido.setTipoEntrega(
                pedidoRequest.getTipoEntrega() != null ? pedidoRequest.getTipoEntrega() : "envio"
        );

        if ("envio".equals(pedido.getTipoEntrega())) {
            pedido.setDireccionEnvio(construirDireccionCompleta(usuario));
            pedido.setCiudadEnvio(usuario.getCiudad());
            pedido.setProvinciaEnvio(usuario.getProvincia());
            pedido.setCodigoPostalEnvio(usuario.getCodigoPostal());
            pedido.setPaisEnvio(usuario.getPais());
        } else {
            pedido.setDireccionEnvio("Barrio Nuevo Jardin, M77 L68");
            pedido.setCiudadEnvio("Córdoba");
            pedido.setProvinciaEnvio("Córdoba");
            pedido.setCodigoPostalEnvio("5000");
            pedido.setPaisEnvio("Argentina");
        }
        pedido.setEmailContacto(usuario.getEmail());
        pedido.setTelefonoContacto(usuario.getTelefono());

        PedidoEntity pedidoGuardado = pedidoRepository.save(pedido);
        log.info("Pedido guardado: {}", pedidoGuardado.getNumeroPedido());

        List<ItemPedidoEntity> items = crearItemsPedido(pedidoGuardado, pedidoRequest.getItems());
        pedidoGuardado.setItems(items);

        // 🔔 AUDITORÍA: Registrar creación del pedido
        registrarAuditoria(null, pedidoGuardado, AccionAuditoria.CREAR, usuario.getId());

        try {
            MercadoPagoResponseDTO mpResponse = mercadoPagoService.crearPreferenciaPago(pedidoGuardado);
            pedidoGuardado.setPreferenciaIdMp(mpResponse.getId());
            log.info("Preferencia de Mercado Pago creada: {}", mpResponse.getId());

            PedidoEntity pedidoActualizado = pedidoRepository.save(pedidoGuardado);

            try {
                String detallesPedido = construirDetallesPedido(items);
                notificacionEventService.enviarNotificacionNuevoPedido(
                        usuario.getEmail(),
                        pedidoActualizado.getNumeroPedido(),
                        detallesPedido,
                        usuario.getId(),
                        usuario.getNombre() + " " + usuario.getApellido()
                );
                log.info("Notificación de nuevo pedido enviada al ADMIN");
            } catch (Exception ex) {
                log.error("Error enviando notificación de nuevo pedido: {}", ex.getMessage());
            }

            return mapToPedidoResponseDTO(pedidoActualizado, mpResponse);
        } catch (Exception e) {
            log.error("Error al crear preferencia de pago", e);
            pedidoRepository.delete(pedidoGuardado);
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage());
        }
    }

    @Transactional
    public PedidoResponseDTO actualizarEstadoPedido(Long id, ActualizarEstadoPedidoDTO actualizarEstadoDTO, String authToken) {
        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        log.info("Actualizando estado del pedido {} de {} a {}",
                pedido.getNumeroPedido(), pedido.getEstado(), actualizarEstadoDTO.getEstado());

        // 🔔 AUDITORÍA: Guardar estado anterior
        PedidoEntity pedidoAnterior = new PedidoEntity();
        modelMapper.map(pedido, pedidoAnterior);

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(actualizarEstadoDTO.getEstado());
        pedido.setFechaActualizacion(LocalDateTime.now());

        PedidoEntity pedidoActualizado = pedidoRepository.save(pedido);

        // 🔔 AUDITORÍA: Registrar modificación (el usuario que hace el cambio viene del token)
        UserResponseDTO usuarioAdmin = userAuthClient.obtenerUsuarioPorEmail(
                pedidoActualizado.getEmailContacto(),
                authToken
        );
        registrarAuditoria(pedidoAnterior, pedidoActualizado, AccionAuditoria.MODIFICAR, usuarioAdmin.getId());

        if (!estadoAnterior.equals(actualizarEstadoDTO.getEstado())) {
            try {
                UserResponseDTO usuario = userAuthClient.obtenerUsuarioPorEmail(
                        pedidoActualizado.getEmailContacto(),
                        authToken
                );

                notificacionEventService.enviarNotificacionEstadoPedido(
                        usuario.getEmail(),
                        usuario.getId(),
                        pedidoActualizado.getNumeroPedido(),
                        actualizarEstadoDTO.getEstado().name(),
                        usuario.getNombre() + " " + usuario.getApellido()
                );

                log.info("Notificación de cambio de estado enviada a usuario {}", usuario.getEmail());
            } catch (Exception e) {
                log.error("Error enviando notificación de cambio de estado: {}", e.getMessage());
            }
        }

        return mapToPedidoResponseDTO(pedidoActualizado);
    }

    private String construirDetallesPedido(List<ItemPedidoEntity> items) {
        if (items.isEmpty()) {
            return "Sin items.";
        }

        StringBuilder detalles = new StringBuilder("Productos: ");
        for (int i = 0; i < Math.min(items.size(), 3); i++) {
            ItemPedidoEntity item = items.get(i);
            detalles.append(item.getProducto().getNombre())
                    .append(" (x").append(item.getCantidad()).append(")");
            if (i < Math.min(items.size(), 3) - 1) {
                detalles.append(", ");
            }
        }

        if (items.size() > 3) {
            detalles.append(" y ").append(items.size() - 3).append(" más");
        }

        return detalles.toString();
    }

    private String obtenerEmailUsuarioDesdeRequest(PedidoRequestDTO pedidoRequest) {
        if (pedidoRequest.getEmailContacto() != null && !pedidoRequest.getEmailContacto().isEmpty()) {
            return pedidoRequest.getEmailContacto();
        }
        throw new RuntimeException("Email del usuario no proporcionado");
    }

    private void validarDireccionUsuario(UserResponseDTO usuario) {
        log.info("Validando dirección del usuario: {}", usuario.getEmail());

        StringBuilder camposFaltantes = new StringBuilder();
        boolean direccionIncompleta = false;

        boolean tieneCalleNumero = (usuario.getCalle() != null && !usuario.getCalle().trim().isEmpty())
                && (usuario.getNumero() != null && !usuario.getNumero().trim().isEmpty());

        boolean tieneManzanaLote = (usuario.getManzana() != null && !usuario.getManzana().trim().isEmpty())
                && (usuario.getLote() != null && !usuario.getLote().trim().isEmpty());

        if (!tieneCalleNumero && !tieneManzanaLote) {
            camposFaltantes.append("Debe completar (calle y número) O (manzana y lote). ");
            direccionIncompleta = true;
        }

        if (usuario.getCiudad() == null || usuario.getCiudad().trim().isEmpty()) {
            camposFaltantes.append("ciudad, ");
            direccionIncompleta = true;
        }

        if (usuario.getProvincia() == null || usuario.getProvincia().trim().isEmpty()) {
            camposFaltantes.append("provincia, ");
            direccionIncompleta = true;
        }

        if (usuario.getPais() == null || usuario.getPais().trim().isEmpty()) {
            camposFaltantes.append("país, ");
            direccionIncompleta = true;
        }

        if (usuario.getCodigoPostal() == null || usuario.getCodigoPostal().trim().isEmpty()) {
            camposFaltantes.append("código postal, ");
            direccionIncompleta = true;
        }

        if (direccionIncompleta) {
            String mensaje = camposFaltantes.toString();
            if (mensaje.endsWith(", ")) {
                mensaje = mensaje.substring(0, mensaje.length() - 2);
            }

            log.warn("Usuario {} intenta crear pedido sin dirección completa. Campos faltantes: {}",
                    usuario.getEmail(), mensaje);

            throw new DireccionIncompletaException(
                    "El usuario no tiene una dirección completa registrada. Complete su perfil antes de realizar un pedido. " +
                            "Campos faltantes: " + mensaje
            );
        }

        log.info("Dirección del usuario {} validada correctamente", usuario.getEmail());
    }

    private String construirDireccionCompleta(UserResponseDTO usuario) {
        StringBuilder direccion = new StringBuilder();

        if (usuario.getCalle() != null && !usuario.getCalle().trim().isEmpty() &&
                usuario.getNumero() != null && !usuario.getNumero().trim().isEmpty()) {

            direccion.append(usuario.getCalle()).append(" ").append(usuario.getNumero());

            if (usuario.getPiso() != null && !usuario.getPiso().trim().isEmpty()) {
                direccion.append(", Piso ").append(usuario.getPiso());
            }

            if (usuario.getBarrio() != null && !usuario.getBarrio().trim().isEmpty()) {
                direccion.append(", ").append(usuario.getBarrio());
            }
        }
        else if (usuario.getManzana() != null && !usuario.getManzana().trim().isEmpty() &&
                usuario.getLote() != null && !usuario.getLote().trim().isEmpty()) {

            direccion.append("Manzana ").append(usuario.getManzana())
                    .append(", Lote ").append(usuario.getLote());

            if (usuario.getBarrio() != null && !usuario.getBarrio().trim().isEmpty()) {
                direccion.append(", ").append(usuario.getBarrio());
            }
        }

        return direccion.toString();
    }

    private BigDecimal calcularTotalPedido(List<ItemPedidoRequestDTO> items) {
        return items.stream()
                .map(item -> {
                    ProductoEntity producto = productoRepository.findById(item.getProductoId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

                    BigDecimal precioConDescuento = producto.getPrecio().subtract(
                            producto.getPrecio().multiply(producto.getDescuentoPorcentaje().divide(BigDecimal.valueOf(100)))
                    );

                    return precioConDescuento.multiply(BigDecimal.valueOf(item.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ItemPedidoEntity> crearItemsPedido(PedidoEntity pedido, List<ItemPedidoRequestDTO> itemsRequest) {
        return itemsRequest.stream()
                .map(itemRequest -> {
                    ProductoEntity producto = productoRepository.findById(itemRequest.getProductoId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemRequest.getProductoId()));

                    OpcionProductoEntity opcion = null;
                    if (itemRequest.getOpcionSeleccionadaId() != null) {
                        opcion = opcionProductoRepository.findById(itemRequest.getOpcionSeleccionadaId())
                                .orElseThrow(() -> new RuntimeException("Opción no encontrada: " + itemRequest.getOpcionSeleccionadaId()));
                    }

                    ItemPedidoEntity item = new ItemPedidoEntity();
                    item.setPedido(pedido);
                    item.setProducto(producto);
                    item.setOpcionSeleccionada(opcion);
                    item.setCantidad(itemRequest.getCantidad());
                    item.setPrecioUnitario(producto.getPrecio());
                    item.setDescuentoPorcentaje(producto.getDescuentoPorcentaje());

                    return itemPedidoRepository.save(item);
                })
                .collect(Collectors.toList());
    }

    public List<PedidoResponseDTO> obtenerPedidosPorUsuario(Long usuarioId, String authToken) {
        List<PedidoEntity> pedidos = pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PedidoResponseDTO> obtenerTodosLosPedidos(String authToken) {
        List<PedidoEntity> pedidos = pedidoRepository.findAllByOrderByFechaCreacionDesc();
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO obtenerPedidoPorId(Long id, String authToken) {
        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        return mapToPedidoResponseDTO(pedido);
    }

    public PedidoResponseDTO obtenerPedidoPorNumero(String numeroPedido, String authToken) {
        PedidoEntity pedido = pedidoRepository.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + numeroPedido));
        return mapToPedidoResponseDTO(pedido);
    }

    @Transactional
    public void procesarWebhook(String preferenciaId, String pagoId, String estadoPago) {
        log.info("Procesando webhook de Mercado Pago - Preferencia: {}, Estado: {}", preferenciaId, estadoPago);

        PedidoEntity pedido = pedidoRepository.findByPreferenciaIdMp(preferenciaId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado para preferencia: " + preferenciaId));

        // 🔔 AUDITORÍA: Guardar estado anterior antes del webhook
        PedidoEntity pedidoAnterior = new PedidoEntity();
        modelMapper.map(pedido, pedidoAnterior);

        pedido.setPagoIdMp(pagoId);
        pedido.setEstadoPagoMp(estadoPago);
        pedido.setFechaActualizacion(LocalDateTime.now());

        switch (estadoPago.toUpperCase()) {
            case "APPROVED":
                pedido.setEstado(EstadoPedido.PAGADO);
                pedido.setFechaPagoMp(LocalDateTime.now());
                limpiarCarrito(pedido.getUsuarioId());
                log.info("Pago aprobado para pedido {}", pedido.getNumeroPedido());
                break;
            case "REJECTED":
                pedido.setEstado(EstadoPedido.CANCELADO);
                log.info("Pago rechazado para pedido {}", pedido.getNumeroPedido());
                break;
            case "PENDING":
                pedido.setEstado(EstadoPedido.PENDIENTE);
                log.info("Pago pendiente para pedido {}", pedido.getNumeroPedido());
                break;
            default:
                pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        }

        PedidoEntity pedidoActualizado = pedidoRepository.save(pedido);

        // 🔔 AUDITORÍA: Registrar cambio por webhook (usuarioId = sistema)
        registrarAuditoria(pedidoAnterior, pedidoActualizado, AccionAuditoria.MODIFICAR, 0L); // 0L = sistema
    }

    // 🔔 NUEVO: Consultar auditoría de pedidos
    public List<AuditoriaPedidoEntity> consultarAuditoriaPedidos() {
        return auditoriaPedidoRepository.findAll();
    }

    // 🔔 NUEVO: Consultar auditoría de un pedido específico
    public List<AuditoriaPedidoEntity> consultarAuditoriaPorPedido(Long pedidoId) {
        return auditoriaPedidoRepository.findByPedidoId(pedidoId);
    }

    private void limpiarCarrito(Long usuarioId) {
        List<CarritoEntity> items = carritoRepository.findByUsuarioId(usuarioId);
        carritoRepository.deleteAll(items);
        log.info("Carrito limpiado para usuario {}", usuarioId);
    }

    private String generarNumeroPedido() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PedidoResponseDTO mapToPedidoResponseDTO(PedidoEntity pedido) {
        PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);

        if (pedido.getItems() != null) {
            List<ItemPedidoResponseDTO> itemsDTO = pedido.getItems().stream()
                    .map(this::mapToItemPedidoResponseDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemsDTO);
        }

        return dto;
    }

    private PedidoResponseDTO mapToPedidoResponseDTO(PedidoEntity pedido, MercadoPagoResponseDTO mpResponse) {
        PedidoResponseDTO dto = mapToPedidoResponseDTO(pedido);
        dto.setInitPoint(mpResponse.getInitPoint());
        dto.setSandboxInitPoint(mpResponse.getSandboxInitPoint());
        return dto;
    }

    private ItemPedidoResponseDTO mapToItemPedidoResponseDTO(ItemPedidoEntity item) {
        ItemPedidoResponseDTO dto = modelMapper.map(item, ItemPedidoResponseDTO.class);

        dto.setProductoId(item.getProducto().getId());
        dto.setNombreProducto(item.getProducto().getNombre());
        dto.setImagenProducto(item.getProducto().getImagen());

        if (item.getOpcionSeleccionada() != null) {
            dto.setOpcionSeleccionadaId(item.getOpcionSeleccionada().getId());
            dto.setTipoOpcion(item.getOpcionSeleccionada().getTipo());
        }

        BigDecimal precioConDescuento = item.getPrecioUnitario().subtract(
                item.getPrecioUnitario().multiply(item.getDescuentoPorcentaje().divide(BigDecimal.valueOf(100)))
        );
        dto.setSubtotal(precioConDescuento.multiply(BigDecimal.valueOf(item.getCantidad())));

        return dto;
    }

    // ---------------------- AUDITORÍA ----------------------

    /**
     * Registra una acción de auditoría para pedidos
     */
    private void registrarAuditoria(PedidoEntity anterior, PedidoEntity nuevo, AccionAuditoria accion, Long usuarioId) {
        AuditoriaPedidoEntity auditoria = new AuditoriaPedidoEntity();

        Long pedidoId = (nuevo != null) ? nuevo.getId() : (anterior != null ? anterior.getId() : null);
        if (pedidoId == null) {
            throw new IllegalStateException("No se puede auditar un pedido sin ID");
        }

        auditoria.setPedidoId(pedidoId);
        auditoria.setAccion(accion);
        auditoria.setUsuarioId(usuarioId);
        auditoria.setFechaAccion(LocalDateTime.now());

        auditoria.setValoresAnteriores(anterior != null ? serializarPedido(anterior) : null);
        auditoria.setValoresNuevos(nuevo != null ? serializarPedido(nuevo) : null);

        auditoriaPedidoRepository.save(auditoria);
        log.info("Auditoría de pedido registrada: acción={}, pedidoId={}, usuarioId={}",
                accion, pedidoId, usuarioId);
    }

    /**
     * Serializa un pedido a JSON para auditoría (evita lazy loading)
     */
    private String serializarPedido(PedidoEntity pedido) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // Para LocalDateTime

            Map<String, Object> data = new HashMap<>();
            data.put("id", pedido.getId());
            data.put("numeroPedido", pedido.getNumeroPedido());
            data.put("usuarioId", pedido.getUsuarioId());
            data.put("total", pedido.getTotal());
            data.put("estado", pedido.getEstado());
            data.put("fechaCreacion", pedido.getFechaCreacion());
            data.put("fechaActualizacion", pedido.getFechaActualizacion());
            data.put("tipoEntrega", pedido.getTipoEntrega());
            data.put("direccionEnvio", pedido.getDireccionEnvio());
            data.put("ciudadEnvio", pedido.getCiudadEnvio());
            data.put("provinciaEnvio", pedido.getProvinciaEnvio());
            data.put("codigoPostalEnvio", pedido.getCodigoPostalEnvio());
            data.put("paisEnvio", pedido.getPaisEnvio());
            data.put("emailContacto", pedido.getEmailContacto());
            data.put("telefonoContacto", pedido.getTelefonoContacto());
            data.put("preferenciaIdMp", pedido.getPreferenciaIdMp());
            data.put("pagoIdMp", pedido.getPagoIdMp());
            data.put("estadoPagoMp", pedido.getEstadoPagoMp());
            data.put("fechaPagoMp", pedido.getFechaPagoMp());

            // Incluir resumen de items (solo IDs y cantidades)
            if (pedido.getItems() != null && !pedido.getItems().isEmpty()) {
                List<Map<String, Object>> itemsResumen = pedido.getItems().stream()
                        .map(item -> {
                            Map<String, Object> itemData = new HashMap<>();
                            itemData.put("productoId", item.getProducto().getId());
                            itemData.put("cantidad", item.getCantidad());
                            itemData.put("precioUnitario", item.getPrecioUnitario());
                            return itemData;
                        })
                        .collect(Collectors.toList());
                data.put("items", itemsResumen);
            }

            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error serializando pedido para auditoría: {}", e.getMessage());
            return "{}";
        }
    }
}
