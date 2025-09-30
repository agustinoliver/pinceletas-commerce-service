package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaProductoRepository extends JpaRepository<AuditoriaProductoEntity, Long> {
    List<AuditoriaProductoEntity> findByProductoId(Long productoId);
}
