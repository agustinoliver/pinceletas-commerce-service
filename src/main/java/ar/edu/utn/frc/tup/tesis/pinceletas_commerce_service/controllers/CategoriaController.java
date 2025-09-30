package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.CategoriaDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaCategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaDTO> crearCategoria(@RequestBody CategoriaDTO categoria,
                                                       @RequestParam Long usuarioId) {
        categoria.setId(null); // Ignora el ID si viene desde el cliente
        CategoriaDTO creada = categoriaService.registrarCategoria(categoria, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> modificarCategoria(@PathVariable Long id,
                                                           @RequestBody CategoriaDTO categoria,
                                                           @RequestParam Long usuarioId) {
        CategoriaDTO actualizada = categoriaService.modificarCategoria(id, categoria, usuarioId);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id,
                                                  @RequestParam Long usuarioId) {
        categoriaService.eliminarCategoria(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ✅ GET /categorias- → Lista con productos incluidos
    @GetMapping("/all-con-products")
    public ResponseEntity<List<CategoriaEntity>> listarCategoriasEntity() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    // ✅ GET /categorias/{id}/entity → Una sola categoría con productos
    @GetMapping("/{id}/one-con-products")
    public ResponseEntity<CategoriaEntity> obtenerCategoriaEntity(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.consultarCategoria(id));
    }


    @GetMapping("/auditoria")
    public ResponseEntity<List<AuditoriaCategoriaEntity>> obtenerAuditoria() {
        return ResponseEntity.ok(categoriaService.consultarAuditoriaCategoria());
    }
}
