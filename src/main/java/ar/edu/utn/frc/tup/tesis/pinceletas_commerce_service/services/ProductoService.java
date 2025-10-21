package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.configs.UserAuthClient;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.UserResponseDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.*;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.AccionAuditoria;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AuditoriaProductoRepository auditoriaProductoRepository;
    private final ModelMapper modelMapper;
    private final OpcionProductoRepository opcionProductoRepository;

    // 🔔 NUEVO: Inyectar dependencias para notificaciones por email
    private final FavoritoRepository favoritoRepository;
    private final EmailService emailService;
    private final UserAuthClient userAuthClient;

    // ---------------------- CREAR PRODUCTO ----------------------

    public ProductoEntity registrarProductoConImagen(ProductoDTO dto, MultipartFile imagenFile, Long usuarioId) {
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(dto.getCategoriaId());

        ProductoEntity producto = new ProductoEntity();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setActivo(dto.getActivo());
        if (dto.getDescuentoPorcentaje() != null) {
            validarDescuento(dto.getDescuentoPorcentaje());
            producto.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        } else {
            producto.setDescuentoPorcentaje(BigDecimal.ZERO);
        }
        producto.setCategoria(categoria);

        // ✅ CORREGIDO: Guardar como lista de imágenes
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String uploadsDir = "uploads/";
                String originalFilename = imagenFile.getOriginalFilename();
                String timestamp = System.currentTimeMillis() + "_";
                String nombreArchivo = timestamp + originalFilename;
                Path filePath = Paths.get(uploadsDir + nombreArchivo);

                Files.createDirectories(filePath.getParent());
                Files.copy(imagenFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // ✅ CORREGIDO: Crear lista con una sola imagen
                List<String> imagenes = new ArrayList<>();
                imagenes.add("/uploads/" + nombreArchivo);
                producto.setImagenes(imagenes);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar imagen", e);
            }
        }

        if (dto.getOpcionesIds() != null && !dto.getOpcionesIds().isEmpty()) {
            List<OpcionProductoEntity> opciones = opcionProductoRepository.findAllById(dto.getOpcionesIds());
            producto.setOpciones(opciones);
        }

        ProductoEntity saved = productoRepository.save(producto);
        registrarAuditoria(null, saved, AccionAuditoria.CREAR, usuarioId);
        return saved;
    }

    // ---------------------- MODIFICAR PRODUCTO ----------------------

    public ProductoDTO modificarProducto(Long id, ProductoDTO dto, Long usuarioId) {
        ProductoEntity original = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoEntity anterior = new ProductoEntity();
        modelMapper.map(original, anterior);

        boolean entraEnDescuento = detectarSiEntraEnDescuento(anterior, dto);

        original.setNombre(dto.getNombre());
        original.setDescripcion(dto.getDescripcion());
        original.setPrecio(dto.getPrecio());
        original.setActivo(dto.getActivo());

        // ✅ CORREGIDO: Actualizar lista de imágenes desde el DTO
        original.setImagenes(dto.getImagenes());

        if (dto.getDescuentoPorcentaje() != null) {
            validarDescuento(dto.getDescuentoPorcentaje());
            original.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        }

        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            original.setCategoria(categoria);
        }

        if (dto.getOpcionesIds() != null) {
            List<OpcionProductoEntity> nuevasOpciones = opcionProductoRepository.findAllById(dto.getOpcionesIds());
            original.setOpciones(nuevasOpciones);
        } else {
            original.setOpciones(null);
        }

        ProductoEntity saved = productoRepository.save(original);
        registrarAuditoria(anterior, saved, AccionAuditoria.MODIFICAR, usuarioId);

        if (entraEnDescuento) {
            notificarDescuentoAFavoritos(saved);
        }

        return mapToDto(saved);
    }

    // ---------------------- MODIFICAR PRODUCTO CON IMAGEN ----------------------

    public ProductoEntity modificarProductoConImagen(Long id, ProductoDTO dto, MultipartFile imagenFile, Long usuarioId) {
        ProductoEntity original = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoEntity anterior = new ProductoEntity();
        modelMapper.map(original, anterior);

        boolean entraEnDescuento = detectarSiEntraEnDescuento(anterior, dto);

        original.setNombre(dto.getNombre());
        original.setDescripcion(dto.getDescripcion());
        original.setPrecio(dto.getPrecio());
        original.setActivo(dto.getActivo());

        if (dto.getDescuentoPorcentaje() != null) {
            validarDescuento(dto.getDescuentoPorcentaje());
            original.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        }

        // ✅ CORREGIDO: Actualizar imagen como lista
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String uploadsDir = "uploads/";
                String originalFilename = imagenFile.getOriginalFilename();
                String timestamp = System.currentTimeMillis() + "_";
                String nombreArchivo = timestamp + originalFilename;
                Path filePath = Paths.get(uploadsDir + nombreArchivo);

                Files.createDirectories(filePath.getParent());
                Files.copy(imagenFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // ✅ CORREGIDO: Reemplazar todas las imágenes por la nueva
                List<String> nuevaImagen = new ArrayList<>();
                nuevaImagen.add("/uploads/" + nombreArchivo);
                original.setImagenes(nuevaImagen);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar imagen", e);
            }
        }

        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            original.setCategoria(categoria);
        }

        if (dto.getOpcionesIds() != null) {
            List<OpcionProductoEntity> nuevasOpciones = opcionProductoRepository.findAllById(dto.getOpcionesIds());
            original.setOpciones(nuevasOpciones);
        } else {
            original.setOpciones(null);
        }

        ProductoEntity saved = productoRepository.save(original);
        registrarAuditoria(anterior, saved, AccionAuditoria.MODIFICAR, usuarioId);

        if (entraEnDescuento) {
            notificarDescuentoAFavoritos(saved);
        }

        return saved;
    }
    // ProductoService.java - Agregar estos métodos

    /**
     * Registra un producto con múltiples imágenes
     */
    public ProductoEntity registrarProductoConMultiplesImagenes(
            ProductoDTO dto, List<MultipartFile> imagenesFiles, Long usuarioId) {

        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        ProductoEntity producto = new ProductoEntity();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setActivo(dto.getActivo());
        producto.setCategoria(categoria);
        producto.setDescuentoPorcentaje(dto.getDescuentoPorcentaje() != null ?
                dto.getDescuentoPorcentaje() : BigDecimal.ZERO);

        // ✅ Procesar múltiples imágenes
        if (imagenesFiles != null && !imagenesFiles.isEmpty()) {
            List<String> rutasImagenes = guardarImagenes(imagenesFiles);
            producto.setImagenes(rutasImagenes);
        }

        // Procesar opciones
        if (dto.getOpcionesIds() != null && !dto.getOpcionesIds().isEmpty()) {
            List<OpcionProductoEntity> opciones = opcionProductoRepository.findAllById(dto.getOpcionesIds());
            producto.setOpciones(opciones);
        }

        ProductoEntity saved = productoRepository.save(producto);
        registrarAuditoria(null, saved, AccionAuditoria.CREAR, usuarioId);
        return saved;
    }

    /**
     * Modifica un producto con múltiples imágenes
     */
    public ProductoEntity modificarProductoConMultiplesImagenes(
            Long id, ProductoDTO dto, List<MultipartFile> imagenesFiles,
            Boolean mantenerImagenes, Long usuarioId) {

        ProductoEntity original = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoEntity anterior = new ProductoEntity();
        modelMapper.map(original, anterior);

        boolean entraEnDescuento = detectarSiEntraEnDescuento(anterior, dto);

        // Actualizar campos básicos
        original.setNombre(dto.getNombre());
        original.setDescripcion(dto.getDescripcion());
        original.setPrecio(dto.getPrecio());
        original.setActivo(dto.getActivo());

        if (dto.getDescuentoPorcentaje() != null) {
            validarDescuento(dto.getDescuentoPorcentaje());
            original.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        }

        // Actualizar categoría
        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            original.setCategoria(categoria);
        }

        // ✅ Actualizar imágenes
        if (imagenesFiles != null && !imagenesFiles.isEmpty()) {
            List<String> nuevasImagenes = guardarImagenes(imagenesFiles);

            if (mantenerImagenes && original.getImagenes() != null) {
                // Agregar las nuevas imágenes a las existentes
                List<String> imagenesActuales = new ArrayList<>(original.getImagenes());
                imagenesActuales.addAll(nuevasImagenes);
                original.setImagenes(imagenesActuales);
            } else {
                // Reemplazar todas las imágenes
                original.setImagenes(nuevasImagenes);
            }
        }

        // Actualizar opciones
        if (dto.getOpcionesIds() != null) {
            List<OpcionProductoEntity> nuevasOpciones = opcionProductoRepository.findAllById(dto.getOpcionesIds());
            original.setOpciones(nuevasOpciones);
        }

        ProductoEntity saved = productoRepository.save(original);
        registrarAuditoria(anterior, saved, AccionAuditoria.MODIFICAR, usuarioId);

        if (entraEnDescuento) {
            notificarDescuentoAFavoritos(saved);
        }

        return saved;
    }

    /**
     * Guarda múltiples imágenes y retorna sus rutas
     */
    private List<String> guardarImagenes(List<MultipartFile> imagenes) {
        List<String> rutasImagenes = new ArrayList<>();
        String uploadsDir = "uploads/";

        try {
            // Limitar a 5 imágenes máximo
            int maxImagenes = Math.min(imagenes.size(), 5);

            for (int i = 0; i < maxImagenes; i++) {
                MultipartFile imagen = imagenes.get(i);
                if (imagen != null && !imagen.isEmpty()) {
                    String nombreOriginal = imagen.getOriginalFilename();
                    String timestamp = System.currentTimeMillis() + "_";
                    String nombreArchivo = timestamp + nombreOriginal;

                    Path filePath = Paths.get(uploadsDir + nombreArchivo);
                    Files.createDirectories(filePath.getParent());
                    Files.copy(imagen.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    rutasImagenes.add("/uploads/" + nombreArchivo);
                }
            }
        } catch (IOException e) {
            log.error("Error al guardar imágenes: {}", e.getMessage());
            throw new RuntimeException("Error al guardar imágenes", e);
        }

        return rutasImagenes;
    }

    /**
     * Elimina una imagen específica de un producto
     */
    public void eliminarImagenDeProducto(Long productoId, int indiceImagen, Long usuarioId) {
        ProductoEntity producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getImagenes() == null || producto.getImagenes().isEmpty()) {
            throw new RuntimeException("El producto no tiene imágenes");
        }

        if (indiceImagen < 0 || indiceImagen >= producto.getImagenes().size()) {
            throw new RuntimeException("Índice de imagen inválido");
        }

        ProductoEntity anterior = new ProductoEntity();
        modelMapper.map(producto, anterior);

        // Eliminar la imagen
        producto.getImagenes().remove(indiceImagen);

        ProductoEntity saved = productoRepository.save(producto);
        registrarAuditoria(anterior, saved, AccionAuditoria.MODIFICAR, usuarioId);
    }

    // ---------------------- ELIMINAR PRODUCTO ----------------------

    public void eliminarProducto(Long id, Long usuarioId) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        registrarAuditoria(producto, null, AccionAuditoria.ELIMINAR, usuarioId);
        productoRepository.delete(producto);
    }

    // ---------------------- CONSULTAS ----------------------

    public ProductoEntity getProductoById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public List<ProductoEntity> listarProductos() {
        return productoRepository.findAll();
    }

    public List<AuditoriaProductoEntity> consultarAuditoriasProductos() {
        return auditoriaProductoRepository.findAll();
    }


    // ---------------------- MAPPERS ----------------------

    private ProductoDTO mapToDto(ProductoEntity entity) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setPrecio(entity.getPrecio());
        dto.setImagenes(entity.getImagenes()); // ✅ CORREGIDO: de setImagen a setImagenes
        dto.setActivo(entity.getActivo());
        dto.setDescuentoPorcentaje(entity.getDescuentoPorcentaje());

        if (entity.getCategoria() != null) {
            dto.setCategoriaId(entity.getCategoria().getId());
        }

        if (entity.getOpciones() != null) {
            List<Long> opcionIds = entity.getOpciones().stream()
                    .map(OpcionProductoEntity::getId)
                    .toList();
            dto.setOpcionesIds(opcionIds);
        }

        return dto;
    }
    private void validarDescuento(BigDecimal descuento) {
        if (descuento.compareTo(BigDecimal.ZERO) < 0 || descuento.compareTo(new BigDecimal(100)) > 0) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
    }

    // ---------------------- AUDITORÍA ----------------------

    private void registrarAuditoria(ProductoEntity anterior, ProductoEntity nuevo, AccionAuditoria accion, Long usuarioId) {
        AuditoriaProductoEntity auditoria = new AuditoriaProductoEntity();

        Long productoId = (nuevo != null) ? nuevo.getId() : (anterior != null ? anterior.getId() : null);
        if (productoId == null) {
            throw new IllegalStateException("No se puede auditar un producto sin ID");
        }

        auditoria.setProductoId(productoId);
        auditoria.setAccion(accion);
        auditoria.setUsuarioId(usuarioId);
        auditoria.setFechaAccion(LocalDateTime.now());

        // Serializar DTOs para evitar problemas con lazy loading
        auditoria.setValoresAnteriores(anterior != null ? serializarProductoPlano(anterior) : null);
        auditoria.setValoresNuevos(nuevo != null ? serializarProductoPlano(nuevo) : null);

        auditoriaProductoRepository.save(auditoria);
    }

    private String serializarProductoPlano(ProductoEntity producto) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            var data = new java.util.HashMap<String, Object>();
            data.put("id", producto.getId());
            data.put("nombre", producto.getNombre());
            data.put("descripcion", producto.getDescripcion());
            data.put("precio", producto.getPrecio());
            data.put("imagenes", producto.getImagenes()); // ✅ CORREGIDO: de imagen a imagenes
            data.put("activo", producto.getActivo());
            data.put("descuentoPorcentaje", producto.getDescuentoPorcentaje());

            if (producto.getCategoria() != null) {
                data.put("categoriaId", producto.getCategoria().getId());
            }

            if (producto.getOpciones() != null) {
                List<Long> opcionIds = producto.getOpciones().stream()
                        .map(OpcionProductoEntity::getId)
                        .collect(Collectors.toList());
                data.put("opcionesIds", opcionIds);
            }

            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ---------------------- NUEVOS MÉTODOS PARA DETECCIÓN Y NOTIFICACIÓN ----------------------

    /**
     * Detecta si un producto entra en descuento
     * (antes NO tenía descuento, ahora SÍ tiene)
     */
    private boolean detectarSiEntraEnDescuento(ProductoEntity anterior, ProductoDTO nuevo) {
        BigDecimal descuentoAnterior = anterior.getDescuentoPorcentaje();
        BigDecimal descuentoNuevo = nuevo.getDescuentoPorcentaje();

        // Considerar null como 0
        if (descuentoAnterior == null) {
            descuentoAnterior = BigDecimal.ZERO;
        }
        if (descuentoNuevo == null) {
            descuentoNuevo = BigDecimal.ZERO;
        }

        // Entra en descuento si:
        // - Antes tenía 0% de descuento
        // - Ahora tiene más de 0% de descuento
        boolean noTeniaDescuento = descuentoAnterior.compareTo(BigDecimal.ZERO) == 0;
        boolean ahoraTieneDescuento = descuentoNuevo.compareTo(BigDecimal.ZERO) > 0;

        return noTeniaDescuento && ahoraTieneDescuento;
    }

    /**
     * Notifica por email a todos los usuarios que tienen este producto en favoritos
     */
    private void notificarDescuentoAFavoritos(ProductoEntity producto) {
        try {
            List<FavoritoEntity> favoritos = favoritoRepository.findByProductoId(producto.getId());

            if (favoritos.isEmpty()) {
                log.info("📭 No hay usuarios con este producto en favoritos: {}", producto.getNombre());
                return;
            }

            log.info("📧 Enviando {} emails de descuento para producto: {}", favoritos.size(), producto.getNombre());

            BigDecimal precioConDescuento = calcularPrecioConDescuento(
                    producto.getPrecio(),
                    producto.getDescuentoPorcentaje()
            );

            for (FavoritoEntity favorito : favoritos) {
                try {
                    UserResponseDTO usuario = userAuthClient.obtenerUsuarioPorId(favorito.getUsuarioId());

                    if (usuario != null && usuario.getEmail() != null) {
                        // ✅ CORREGIDO: Usar imagen principal en lugar de imagen única
                        String imagenPrincipal = producto.getImagenPrincipal();

                        emailService.enviarEmailDescuentoFavorito(
                                usuario.getEmail(),
                                usuario.getNombre() + " " + usuario.getApellido(),
                                producto.getNombre(),
                                producto.getPrecio(),
                                producto.getDescuentoPorcentaje(),
                                precioConDescuento,
                                imagenPrincipal, // ✅ Usar imagen principal
                                producto.getId()
                        );

                        log.info("✅ Email enviado a: {} para producto: {}", usuario.getEmail(), producto.getNombre());
                    }
                } catch (Exception e) {
                    log.error("❌ Error enviando email a usuario {}: {}", favorito.getUsuarioId(), e.getMessage());
                }
            }

            log.info("✅ Proceso de notificación de descuentos completado para: {}", producto.getNombre());

        } catch (Exception e) {
            log.error("❌ Error general en notificación de descuentos: {}", e.getMessage(), e);
        }
    }

    /**
     * Calcula el precio con descuento aplicado
     */
    private BigDecimal calcularPrecioConDescuento(BigDecimal precio, BigDecimal descuentoPorcentaje) {
        if (descuentoPorcentaje == null || descuentoPorcentaje.compareTo(BigDecimal.ZERO) == 0) {
            return precio;
        }

        BigDecimal descuento = precio.multiply(descuentoPorcentaje.divide(BigDecimal.valueOf(100)));
        return precio.subtract(descuento);
    }

    /**
     * Obtiene el email de un usuario por su ID
     * (consulta al microservicio user-auth)
     */
    private String obtenerEmailPorUsuarioId(Long usuarioId) {
        try {
            UserResponseDTO usuario = userAuthClient.obtenerUsuarioPorId(usuarioId);
            return usuario.getEmail();
        } catch (Exception e) {
            log.error("❌ Error obteniendo email para usuarioId {}: {}", usuarioId, e.getMessage());
            throw new RuntimeException("Error al obtener email del usuario: " + e.getMessage());
        }
    }



}