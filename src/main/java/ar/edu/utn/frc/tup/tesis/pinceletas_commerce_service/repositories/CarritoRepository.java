package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CarritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarritoRepository extends JpaRepository<CarritoEntity, Long> {
    List<CarritoEntity> findByUsuarioId(Long usuarioId);

    // ✅ NUEVO: Método para verificar si ya existe un producto con opción específica
    boolean existsByUsuarioIdAndProductoIdAndOpcionSeleccionadaId(Long usuarioId, Long productoId, Long opcionSeleccionadaId);

    // ✅ NUEVO: Método para productos sin opción
    boolean existsByUsuarioIdAndProductoIdAndOpcionSeleccionadaIsNull(Long usuarioId, Long productoId);

}
