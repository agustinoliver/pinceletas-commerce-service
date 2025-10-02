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

    // ✅ POST CON IMAGEN - VERSIÓN CON CAMPOS INDIVIDUALES
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
                .imagen("") // Se llenará en el servicio
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


//    @PostMapping
//    public ResponseEntity<ProductoEntity> crearProducto(@RequestBody ProductoDTO dto, @RequestParam Long usuarioId) {
//        ProductoEntity creado = productoService.registrarProducto(dto, usuarioId); // nuevo método
//        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
//    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoEntity> modificarProducto(@PathVariable Long id,
                                                            @RequestBody ProductoDTO dto,
                                                            @RequestParam Long usuarioId) {
        productoService.modificarProducto(id, dto, usuarioId);
        ProductoEntity actualizado = productoService.getProductoById(id);
        return ResponseEntity.ok(actualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id,
                                                 @RequestParam Long usuarioId) {
        productoService.eliminarProducto(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoEntity> obtenerProducto(@PathVariable Long id) {
        ProductoEntity producto = productoService.getProductoById(id);
        return ResponseEntity.ok(producto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProductoEntity>> listarProductos() {
        List<ProductoEntity> productos = productoService.listarProductos();
        return ResponseEntity.ok(productos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/auditoria")
    public ResponseEntity<List<AuditoriaProductoEntity>> obtenerAuditoria(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.consultarAuditoriaProducto(id));
    }
}
