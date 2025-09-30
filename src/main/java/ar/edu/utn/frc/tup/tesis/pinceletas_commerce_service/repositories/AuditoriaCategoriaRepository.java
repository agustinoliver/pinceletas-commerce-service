package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaCategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaCategoriaRepository extends JpaRepository<AuditoriaCategoriaEntity, Long> {
    List<AuditoriaCategoriaEntity> findByCategoriaId(Long categoriaId);
}
