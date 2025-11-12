package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    @Autowired
    private final ProductoService productoService;

    // ✅ POST CON IMAGEN - VERSIÓN CON CAMPOS INDIVIDUALES (MANTENIDO PARA COMPATIBILIDAD)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(value = "/productos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear producto con imagen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado")
    })
    public ResponseEntity<ProductoEntity> crearProductoConImagen(
            @Parameter(description = "Nombre del producto", required = true)
            @RequestParam("nombre") String nombre,

            @Parameter(description = "Descripción del producto")
            @RequestParam(value = "descripcion", required = false) String descripcion,

            @Parameter(description = "Precio del producto", required = true)
            @RequestParam("precio") BigDecimal precio,

            @Parameter(description = "Descuento en porcentaje (0-100)")
            @RequestParam(value = "descuentoPorcentaje", required = false) BigDecimal descuentoPorcentaje,

            @Parameter(description = "Si el producto está activo", required = true)
            @RequestParam("activo") Boolean activo,

            @Parameter(description = "ID de la categoría", required = true)
            @RequestParam("categoriaId") Long categoriaId,

            @Parameter(description = "IDs de las opciones (separados por coma, ej: 1,2,3)")
            @RequestParam(value = "opcionesIds", required = false) String opcionesIdsStr,

            @Parameter(description = "Imagen del producto", required = false)
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,

            @Parameter(description = "ID del usuario", required = true)
            @RequestParam Long usuarioId) {

        // Construir el DTO manualmente
        ProductoDTO producto = ProductoDTO.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .activo(activo)
                .categoriaId(categoriaId)
                .descuentoPorcentaje(descuentoPorcentaje != null ? descuentoPorcentaje : BigDecimal.ZERO)
                // ✅ CORREGIDO: No setear imagen, se manejará en el servicio
                .build();

        // Procesar opcionesIds si existen
        if (opcionesIdsStr != null && !opcionesIdsStr.trim().isEmpty()) {
            List<Long> opcionesIds = Arrays.stream(opcionesIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            producto.setOpcionesIds(opcionesIds);
        }

        ProductoEntity creado = productoService.registrarProductoConImagen(producto, imagen, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ✅ PUT CON IMAGEN - NUEVO ENDPOINT PARA ACTUALIZAR CON IMAGEN (MANTENIDO PARA COMPATIBILIDAD)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}/con-imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar producto con imagen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoEntity> modificarProductoConImagen(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long id,

            @Parameter(description = "Nombre del producto", required = true)
            @RequestParam("nombre") String nombre,

            @Parameter(description = "Descripción del producto")
            @RequestParam(value = "descripcion", required = false) String descripcion,

            @Parameter(description = "Precio del producto", required = true)
            @RequestParam("precio") BigDecimal precio,

            @Parameter(description = "Descuento en porcentaje (0-100)")
            @RequestParam(value = "descuentoPorcentaje", required = false) BigDecimal descuentoPorcentaje,

            @Parameter(description = "Si el producto está activo", required = true)
            @RequestParam("activo") Boolean activo,

            @Parameter(description = "ID de la categoría", required = true)
            @RequestParam("categoriaId") Long categoriaId,

            @Parameter(description = "IDs de las opciones (separados por coma, ej: 1,2,3)")
            @RequestParam(value = "opcionesIds", required = false) String opcionesIdsStr,

            @Parameter(description = "Imagen del producto", required = false)
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,

            @Parameter(description = "ID del usuario", required = true)
            @RequestParam Long usuarioId) {

        // Construir el DTO manualmente
        ProductoDTO producto = ProductoDTO.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .activo(activo)
                .categoriaId(categoriaId)
                .descuentoPorcentaje(descuentoPorcentaje != null ? descuentoPorcentaje : BigDecimal.ZERO)
                // ✅ CORREGIDO: No setear imagen, se manejará en el servicio
                .build();

        // Procesar opcionesIds si existen
        if (opcionesIdsStr != null && !opcionesIdsStr.trim().isEmpty()) {
            List<Long> opcionesIds = Arrays.stream(opcionesIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            producto.setOpcionesIds(opcionesIds);
        }

        ProductoEntity actualizado = productoService.modificarProductoConImagen(id, producto, imagen, usuarioId);
        return ResponseEntity.ok(actualizado);
    }

    // ✅ PUT SIN IMAGEN - ACTUALIZAR SOLO DATOS
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoEntity> modificarProducto(@PathVariable Long id,
                                                            @RequestBody ProductoDTO dto,
                                                            @RequestParam Long usuarioId) {
        productoService.modificarProducto(id, dto, usuarioId);
        ProductoEntity actualizado = productoService.getProductoById(id);
        return ResponseEntity.ok(actualizado);
    }

    // ✅ DELETE PRODUCTO
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id,
                                                 @RequestParam Long usuarioId) {
        productoService.eliminarProducto(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ✅ OBTENER PRODUCTO POR ID

    @GetMapping("/{id}")
    public ResponseEntity<ProductoEntity> obtenerProducto(@PathVariable Long id) {
        ProductoEntity producto = productoService.getProductoById(id);
        return ResponseEntity.ok(producto);
    }

    // ✅ LISTAR TODOS LOS PRODUCTOS

    @GetMapping
    public ResponseEntity<List<ProductoEntity>> listarProductos() {
        List<ProductoEntity> productos = productoService.listarProductos();
        return ResponseEntity.ok(productos);
    }

    // ✅ OBTENER AUDITORÍAS
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auditorias")
    public ResponseEntity<List<AuditoriaProductoEntity>> obtenerAuditorias() {
        return ResponseEntity.ok(productoService.consultarAuditoriasProductos());
    }

    // ✅ NUEVO: CREAR PRODUCTO CON MÚLTIPLES IMÁGENES
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping(value = "/productos-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear producto con múltiples imágenes")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado con múltiples imágenes")
    })
    public ResponseEntity<ProductoEntity> crearProductoConMultiplesImagenes(
            @Parameter(description = "Nombre del producto", required = true)
            @RequestParam("nombre") String nombre,

            @Parameter(description = "Descripción del producto")
            @RequestParam(value = "descripcion", required = false) String descripcion,

            @Parameter(description = "Precio del producto", required = true)
            @RequestParam("precio") BigDecimal precio,

            @Parameter(description = "Descuento en porcentaje (0-100)")
            @RequestParam(value = "descuentoPorcentaje", required = false) BigDecimal descuentoPorcentaje,

            @Parameter(description = "Si el producto está activo", required = true)
            @RequestParam("activo") Boolean activo,

            @Parameter(description = "ID de la categoría", required = true)
            @RequestParam("categoriaId") Long categoriaId,

            @Parameter(description = "IDs de las opciones (separados por coma)")
            @RequestParam(value = "opcionesIds", required = false) String opcionesIdsStr,

            @Parameter(description = "Imágenes del producto (hasta 5)", required = false)
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,

            @Parameter(description = "ID del usuario", required = true)
            @RequestParam Long usuarioId) {

        ProductoDTO producto = ProductoDTO.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .activo(activo)
                .categoriaId(categoriaId)
                .descuentoPorcentaje(descuentoPorcentaje != null ? descuentoPorcentaje : BigDecimal.ZERO)
                .build();

        if (opcionesIdsStr != null && !opcionesIdsStr.trim().isEmpty()) {
            List<Long> opcionesIds = Arrays.stream(opcionesIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            producto.setOpcionesIds(opcionesIds);
        }

        ProductoEntity creado = productoService.registrarProductoConMultiplesImagenes(producto, imagenes, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ✅ NUEVO: ACTUALIZAR PRODUCTO CON MÚLTIPLES IMÁGENES
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}/con-multiples-imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar producto con múltiples imágenes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado con múltiples imágenes"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoEntity> modificarProductoConMultiplesImagenes(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long id,

            @Parameter(description = "Nombre del producto", required = true)
            @RequestParam("nombre") String nombre,

            @Parameter(description = "Descripción del producto")
            @RequestParam(value = "descripcion", required = false) String descripcion,

            @Parameter(description = "Precio del producto", required = true)
            @RequestParam("precio") BigDecimal precio,

            @Parameter(description = "Descuento en porcentaje (0-100)")
            @RequestParam(value = "descuentoPorcentaje", required = false) BigDecimal descuentoPorcentaje,

            @Parameter(description = "Si el producto está activo", required = true)
            @RequestParam("activo") Boolean activo,

            @Parameter(description = "ID de la categoría", required = true)
            @RequestParam("categoriaId") Long categoriaId,

            @Parameter(description = "IDs de las opciones (separados por coma)")
            @RequestParam(value = "opcionesIds", required = false) String opcionesIdsStr,

            @Parameter(description = "Imágenes del producto (hasta 5)", required = false)
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,

            @Parameter(description = "Si se deben mantener las imágenes existentes", required = false)
            @RequestParam(value = "mantenerImagenes", required = false, defaultValue = "true") Boolean mantenerImagenes,

            @Parameter(description = "ID del usuario", required = true)
            @RequestParam Long usuarioId) {

        ProductoDTO producto = ProductoDTO.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .precio(precio)
                .activo(activo)
                .categoriaId(categoriaId)
                .descuentoPorcentaje(descuentoPorcentaje != null ? descuentoPorcentaje : BigDecimal.ZERO)
                .build();

        if (opcionesIdsStr != null && !opcionesIdsStr.trim().isEmpty()) {
            List<Long> opcionesIds = Arrays.stream(opcionesIdsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            producto.setOpcionesIds(opcionesIds);
        }

        ProductoEntity actualizado = productoService.modificarProductoConMultiplesImagenes(
                id, producto, imagenes, mantenerImagenes, usuarioId);
        return ResponseEntity.ok(actualizado);
    }

    // ✅ NUEVO: ENDPOINT PARA ELIMINAR UNA IMAGEN ESPECÍFICA
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productoId}/imagenes/{indiceImagen}")
    @Operation(summary = "Eliminar una imagen específica de un producto")
    public ResponseEntity<Void> eliminarImagenDeProducto(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId,

            @Parameter(description = "Índice de la imagen a eliminar (0-based)", required = true)
            @PathVariable int indiceImagen,

            @Parameter(description = "ID del usuario", required = true)
            @RequestParam Long usuarioId) {

        productoService.eliminarImagenDeProducto(productoId, indiceImagen, usuarioId);
        return ResponseEntity.noContent().build();
    }
}