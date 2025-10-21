package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaPedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaPedidoRepository extends JpaRepository<AuditoriaPedidoEntity, Long> {
    List<AuditoriaPedidoEntity> findByPedidoId(Long pedidoId);
}
