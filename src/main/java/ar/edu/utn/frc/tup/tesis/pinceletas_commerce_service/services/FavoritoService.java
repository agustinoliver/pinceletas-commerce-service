package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.FavoritoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.FavoritoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.FavoritoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ProductoRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProductoRepository productoRepository;

    public FavoritoEntity agregarAFavoritos(FavoritoDTO dto) {
        Long usuarioId = dto.getUsuarioId();
        Long productoId = dto.getProductoId();

        if (favoritoRepository.existsByUsuarioIdAndProductoId(usuarioId, productoId)) {
            throw new RuntimeException("Ya está en favoritos");
        }

        ProductoEntity producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        FavoritoEntity favorito = new FavoritoEntity();
        favorito.setUsuarioId(usuarioId);
        favorito.setProducto(producto);

        return favoritoRepository.save(favorito);
    }

    @Transactional
    public void eliminarDeFavoritos(Long usuarioId, Long productoId) {
        favoritoRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
    }

    public List<FavoritoEntity> obtenerFavoritos(Long usuarioId) {
        return favoritoRepository.findByUsuarioId(usuarioId);
    }
}
