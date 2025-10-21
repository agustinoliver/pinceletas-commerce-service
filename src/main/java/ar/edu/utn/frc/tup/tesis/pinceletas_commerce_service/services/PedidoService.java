package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.configs.UserAuthClient;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.*;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.*;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.EstadoPedido;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.exceptions.DireccionIncompletaException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO pedidoRequest, String authToken) {
        log.info("Iniciando creación de pedido");

        // Obtener datos del usuario desde user-auth-service
        UserResponseDTO usuario = userAuthClient.obtenerUsuarioPorEmail(
                obtenerEmailUsuarioDesdeRequest(pedidoRequest),
                authToken
        );
        log.info("Usuario obtenido: {}", usuario.getEmail());

        // Validar que el usuario tenga dirección completa
        validarDireccionUsuario(usuario);

        // Validar productos y calcular total
        BigDecimal total = calcularTotalPedido(pedidoRequest.getItems());
        log.info("Total del pedido calculado: {}", total);

        // Crear pedido
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

        // Guardar snapshot de los datos de envío y contacto
        if ("envio".equals(pedido.getTipoEntrega())) {
            pedido.setDireccionEnvio(construirDireccionCompleta(usuario));
            pedido.setCiudadEnvio(usuario.getCiudad());
            pedido.setProvinciaEnvio(usuario.getProvincia());
            pedido.setCodigoPostalEnvio(usuario.getCodigoPostal());
            pedido.setPaisEnvio(usuario.getPais());
        } else {
            // Para retiro en local, guardar los datos del local
            pedido.setDireccionEnvio("Barrio Nuevo Jardin, M77 L68");
            pedido.setCiudadEnvio("Córdoba");
            pedido.setProvinciaEnvio("Córdoba");
            pedido.setCodigoPostalEnvio("5000");
            pedido.setPaisEnvio("Argentina");
        }
        pedido.setEmailContacto(usuario.getEmail());
        pedido.setTelefonoContacto(usuario.getTelefono());

        // Guardar pedido primero (sin items ni preferencia MP)
        PedidoEntity pedidoGuardado = pedidoRepository.save(pedido);
        log.info("Pedido guardado: {}", pedidoGuardado.getNumeroPedido());

        // Crear items del pedido
        List<ItemPedidoEntity> items = crearItemsPedido(pedidoGuardado, pedidoRequest.getItems());
        pedidoGuardado.setItems(items);

        // Crear preferencia de pago en Mercado Pago
        try {
            MercadoPagoResponseDTO mpResponse = mercadoPagoService.crearPreferenciaPago(pedidoGuardado);
            pedidoGuardado.setPreferenciaIdMp(mpResponse.getId());
            log.info("Preferencia de Mercado Pago creada: {}", mpResponse.getId());

            PedidoEntity pedidoActualizado = pedidoRepository.save(pedidoGuardado);
            return mapToPedidoResponseDTO(pedidoActualizado, mpResponse);
        } catch (Exception e) {
            log.error("Error al crear preferencia de pago", e);
            // Si falla Mercado Pago, eliminamos el pedido
            pedidoRepository.delete(pedidoGuardado);
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage());
        }
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

        // ✅ VALIDAR DIRECCIÓN: Debe tener (calle + número) O (manzana + lote)
        boolean tieneCalleNumero = (usuario.getCalle() != null && !usuario.getCalle().trim().isEmpty())
                && (usuario.getNumero() != null && !usuario.getNumero().trim().isEmpty());

        boolean tieneManzanaLote = (usuario.getManzana() != null && !usuario.getManzana().trim().isEmpty())
                && (usuario.getLote() != null && !usuario.getLote().trim().isEmpty());

        if (!tieneCalleNumero && !tieneManzanaLote) {
            camposFaltantes.append("Debe completar (calle y número) O (manzana y lote). ");
            direccionIncompleta = true;
        }

        // ✅ VALIDAR CIUDAD (obligatorio)
        if (usuario.getCiudad() == null || usuario.getCiudad().trim().isEmpty()) {
            camposFaltantes.append("ciudad, ");
            direccionIncompleta = true;
        }

        // ✅ VALIDAR PROVINCIA (obligatorio)
        if (usuario.getProvincia() == null || usuario.getProvincia().trim().isEmpty()) {
            camposFaltantes.append("provincia, ");
            direccionIncompleta = true;
        }

        // ✅ VALIDAR PAÍS (obligatorio)
        if (usuario.getPais() == null || usuario.getPais().trim().isEmpty()) {
            camposFaltantes.append("país, ");
            direccionIncompleta = true;
        }

        // ✅ VALIDAR CÓDIGO POSTAL (obligatorio)
        if (usuario.getCodigoPostal() == null || usuario.getCodigoPostal().trim().isEmpty()) {
            camposFaltantes.append("código postal, ");
            direccionIncompleta = true;
        }

        if (direccionIncompleta) {
            // Eliminar la última coma y espacio si existe
            String mensaje = camposFaltantes.toString();
            if (mensaje.endsWith(", ")) {
                mensaje = mensaje.substring(0, mensaje.length() - 2);
            }

            log.warn("Usuario {} intenta crear pedido sin dirección completa. Campos faltantes: {}",
                    usuario.getEmail(), mensaje);

            // Lanzar la excepción personalizada con mensaje detallado
            throw new DireccionIncompletaException(
                    "El usuario no tiene una dirección completa registrada. Complete su perfil antes de realizar un pedido. " +
                            "Campos faltantes: " + mensaje
            );
        }

        log.info("Dirección del usuario {} validada correctamente", usuario.getEmail());
    }

    private String construirDireccionCompleta(UserResponseDTO usuario) {
        StringBuilder direccion = new StringBuilder();

        // ✅ PRIORIZAR CALLE + NÚMERO si están disponibles
        if (usuario.getCalle() != null && !usuario.getCalle().trim().isEmpty() &&
                usuario.getNumero() != null && !usuario.getNumero().trim().isEmpty()) {

            direccion.append(usuario.getCalle()).append(" ").append(usuario.getNumero());

            // Agregar piso si existe
            if (usuario.getPiso() != null && !usuario.getPiso().trim().isEmpty()) {
                direccion.append(", Piso ").append(usuario.getPiso());
            }

            // Agregar barrio si existe
            if (usuario.getBarrio() != null && !usuario.getBarrio().trim().isEmpty()) {
                direccion.append(", ").append(usuario.getBarrio());
            }
        }
        // ✅ SI NO, USAR MANZANA + LOTE
        else if (usuario.getManzana() != null && !usuario.getManzana().trim().isEmpty() &&
                usuario.getLote() != null && !usuario.getLote().trim().isEmpty()) {

            direccion.append("Manzana ").append(usuario.getManzana())
                    .append(", Lote ").append(usuario.getLote());

            // Agregar barrio si existe
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
    public PedidoResponseDTO actualizarEstadoPedido(Long id, ActualizarEstadoPedidoDTO actualizarEstadoDTO, String authToken) {
        PedidoEntity pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        log.info("Actualizando estado del pedido {} de {} a {}", pedido.getNumeroPedido(), pedido.getEstado(), actualizarEstadoDTO.getEstado());

        pedido.setEstado(actualizarEstadoDTO.getEstado());
        pedido.setFechaActualizacion(LocalDateTime.now());

        PedidoEntity pedidoActualizado = pedidoRepository.save(pedido);
        return mapToPedidoResponseDTO(pedidoActualizado);
    }

    @Transactional
    public void procesarWebhook(String preferenciaId, String pagoId, String estadoPago) {
        log.info("Procesando webhook de Mercado Pago - Preferencia: {}, Estado: {}", preferenciaId, estadoPago);

        PedidoEntity pedido = pedidoRepository.findByPreferenciaIdMp(preferenciaId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado para preferencia: " + preferenciaId));

        pedido.setPagoIdMp(pagoId);
        pedido.setEstadoPagoMp(estadoPago);
        pedido.setFechaActualizacion(LocalDateTime.now());

        // Actualizar estado del pedido según el estado del pago
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

        pedidoRepository.save(pedido);
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
}
