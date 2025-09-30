package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.FavoritoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.FavoritoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    // Registrar producto en favoritos
    @PostMapping
    public ResponseEntity<FavoritoEntity> agregarAFavoritos(@RequestBody FavoritoDTO favoritoDTO) {
        FavoritoEntity favorito = favoritoService.agregarAFavoritos(favoritoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(favorito);
    }


    // Eliminar producto de favoritos
    @DeleteMapping
    public ResponseEntity<Void> eliminarDeFavoritos(@RequestParam Long usuarioId,
                                                    @RequestParam Long productoId) {
        favoritoService.eliminarDeFavoritos(usuarioId, productoId);
        return ResponseEntity.noContent().build();
    }

    // Consultar productos favoritos por usuario
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<FavoritoEntity>> obtenerFavoritos(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(favoritoService.obtenerFavoritos(usuarioId));
    }
}
