package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.CarritoRequestDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CarritoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    // Registrar producto en carrito
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<CarritoEntity> agregarProducto(
            @RequestParam Long usuarioId,
            @RequestBody CarritoRequestDTO request) {
        CarritoEntity item = carritoService.agregarProducto(usuarioId, request.getProductoId(), request.getCantidad());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }


    // Modificar cantidad de un producto en el carrito
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{itemId}")
    public ResponseEntity<CarritoEntity> modificarItem(@PathVariable Long itemId,
                                                       @RequestParam int nuevaCantidad) {
        CarritoEntity actualizado = carritoService.modificarItem(itemId, nuevaCantidad);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un producto del carrito
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long itemId) {
        carritoService.eliminarItem(itemId);
        return ResponseEntity.noContent().build();
    }

    // Consultar carrito completo por usuario
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<CarritoEntity>> obtenerCarrito(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(usuarioId));
    }

    // Detalle del carrito de compras (total a pagar)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{usuarioId}/total")
    public ResponseEntity<BigDecimal> calcularTotal(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carritoService.calcularTotalCarrito(usuarioId));
    }
}
