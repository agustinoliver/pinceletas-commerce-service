package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.PedidoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
    List<PedidoEntity> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<PedidoEntity> findAllByOrderByFechaCreacionDesc();
    Optional<PedidoEntity> findByNumeroPedido(String numeroPedido);
    Optional<PedidoEntity> findByPreferenciaIdMp(String preferenciaIdMp);
    List<PedidoEntity> findByEstado(EstadoPedido estado);
}
