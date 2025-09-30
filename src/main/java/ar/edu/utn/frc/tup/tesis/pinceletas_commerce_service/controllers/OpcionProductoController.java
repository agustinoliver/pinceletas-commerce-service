package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.OpcionProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.OpcionProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/opciones-productos")
@RequiredArgsConstructor
public class OpcionProductoController {

    private final OpcionProductoService opcionService;

    @PostMapping
    public ResponseEntity<OpcionProductoDTO> crearOpcion(@RequestBody OpcionProductoDTO opcionDto) {
        OpcionProductoDTO creada = opcionService.crearOpcion(opcionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpcionProductoDTO> modificarOpcion(@PathVariable Long id,
                                                             @RequestBody OpcionProductoDTO opcionDto) {
        OpcionProductoDTO actualizada = opcionService.modificarOpcion(id, opcionDto);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOpcion(@PathVariable Long id) {
        opcionService.eliminarOpcion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<OpcionProductoDTO>> obtenerOpcionesPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(opcionService.obtenerPorProducto(productoId));
    }

    @GetMapping
    public ResponseEntity<List<OpcionProductoDTO>> listarTodasOpciones() {
        return ResponseEntity.ok(opcionService.listarTodas());
    }
}
