package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.OpcionProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.AccionAuditoria;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.AuditoriaProductoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.CategoriaRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.OpcionProductoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@AllArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AuditoriaProductoRepository auditoriaProductoRepository;
    private final ModelMapper modelMapper;
    private final OpcionProductoRepository opcionProductoRepository;

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

        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String uploadsDir = "uploads/";
                String originalFilename = imagenFile.getOriginalFilename();
                Path filePath = Paths.get(uploadsDir + originalFilename);

                Files.createDirectories(filePath.getParent());
                Files.copy(imagenFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                producto.setImagen("/uploads/" + originalFilename);
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
        modelMapper.map(original, anterior); // copia para auditoría

        // Modificamos manualmente los campos que vienen del DTO
        original.setNombre(dto.getNombre());
        original.setDescripcion(dto.getDescripcion());
        original.setPrecio(dto.getPrecio());
        original.setImagen(dto.getImagen());
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

        // ✅ Actualizar opciones (ManyToMany: se setea la lista completa)
        if (dto.getOpcionesIds() != null) {
            List<OpcionProductoEntity> nuevasOpciones = opcionProductoRepository.findAllById(dto.getOpcionesIds());
            original.setOpciones(nuevasOpciones);
        } else {
            original.setOpciones(null);
        }

        ProductoEntity saved = productoRepository.save(original);
        registrarAuditoria(anterior, saved, AccionAuditoria.MODIFICAR, usuarioId);
        return mapToDto(saved);
    }

    // ---------------------- MODIFICAR PRODUCTO CON IMAGEN ----------------------

    public ProductoEntity modificarProductoConImagen(Long id, ProductoDTO dto, MultipartFile imagenFile, Long usuarioId) {
        ProductoEntity original = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoEntity anterior = new ProductoEntity();
        modelMapper.map(original, anterior); // copia para auditoría

        // Modificamos manualmente los campos que vienen del DTO
        original.setNombre(dto.getNombre());
        original.setDescripcion(dto.getDescripcion());
        original.setPrecio(dto.getPrecio());
        original.setActivo(dto.getActivo());

        if (dto.getDescuentoPorcentaje() != null) {
            validarDescuento(dto.getDescuentoPorcentaje());
            original.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        }

        // Actualizar imagen si se proporciona
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String uploadsDir = "uploads/";
                String originalFilename = imagenFile.getOriginalFilename();
                Path filePath = Paths.get(uploadsDir + originalFilename);

                Files.createDirectories(filePath.getParent());
                Files.copy(imagenFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                original.setImagen("/uploads/" + originalFilename);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar imagen", e);
            }
        }

        // Actualizar categoría
        if (dto.getCategoriaId() != null) {
            CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            original.setCategoria(categoria);
        }

        // ✅ Actualizar opciones (ManyToMany: se setea la lista completa)
        if (dto.getOpcionesIds() != null) {
            List<OpcionProductoEntity> nuevasOpciones = opcionProductoRepository.findAllById(dto.getOpcionesIds());
            original.setOpciones(nuevasOpciones);
        } else {
            original.setOpciones(null);
        }

        ProductoEntity saved = productoRepository.save(original);
        registrarAuditoria(anterior, saved, AccionAuditoria.MODIFICAR, usuarioId);
        return saved;
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
        dto.setId(entity.getId()); // ESTA LÍNEA ES FUNDAMENTAL
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setPrecio(entity.getPrecio());
        dto.setImagen(entity.getImagen());
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

            // Crear un DTO plano
            var data = new java.util.HashMap<String, Object>();
            data.put("id", producto.getId());
            data.put("nombre", producto.getNombre());
            data.put("descripcion", producto.getDescripcion());
            data.put("precio", producto.getPrecio());
            data.put("imagen", producto.getImagen());
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
}