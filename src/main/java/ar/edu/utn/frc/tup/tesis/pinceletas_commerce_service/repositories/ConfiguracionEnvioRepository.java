package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ConfiguracionEnvioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionEnvioRepository extends JpaRepository<ConfiguracionEnvioEntity, Long> {

    List<ConfiguracionEnvioEntity> findByActivoTrue();

    Optional<ConfiguracionEnvioEntity> findFirstByActivoTrue();

    @Query("SELECT c FROM ConfiguracionEnvioEntity c WHERE c.activo = true ORDER BY c.fechaCreacion DESC")
    Optional<ConfiguracionEnvioEntity> findLatestActive();
}