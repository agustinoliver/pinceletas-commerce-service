package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.OpcionProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpcionProductoRepository extends JpaRepository<OpcionProductoEntity, Long> {
    List<OpcionProductoEntity> findByProductoId(Long productoId);
}
