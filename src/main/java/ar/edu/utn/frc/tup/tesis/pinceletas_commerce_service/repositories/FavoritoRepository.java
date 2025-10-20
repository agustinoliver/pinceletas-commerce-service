package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.FavoritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritoRepository extends JpaRepository<FavoritoEntity, Long> {
    List<FavoritoEntity> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    // 🔔 NUEVO: Obtener todos los favoritos de un producto específico
    List<FavoritoEntity> findByProductoId(Long productoId);
}
