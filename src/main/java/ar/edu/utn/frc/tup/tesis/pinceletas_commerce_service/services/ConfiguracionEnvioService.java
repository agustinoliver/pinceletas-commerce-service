package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ConfiguracionEnvioRequestDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ConfiguracionEnvioResponseDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ConfiguracionEnvioEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ConfiguracionEnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionEnvioService {
    private final ConfiguracionEnvioRepository configuracionEnvioRepository;
    private final ModelMapper modelMapper;

    /**
     * Obtiene la configuración activa actual del envío
     */
    public ConfiguracionEnvioResponseDTO obtenerConfiguracionActiva() {
        Optional<ConfiguracionEnvioEntity> configuracionOpt = configuracionEnvioRepository.findLatestActive();

        if (configuracionOpt.isPresent()) {
            return modelMapper.map(configuracionOpt.get(), ConfiguracionEnvioResponseDTO.class);
        }

        // Si no hay configuración, crear una por defecto
        log.info("No se encontró configuración activa, creando configuración por defecto");
        return crearConfiguracionPorDefecto();
    }

    /**
     * Obtiene el costo de envío aplicable según el subtotal
     */
    public BigDecimal obtenerCostoEnvio(BigDecimal subtotal) {
        ConfiguracionEnvioResponseDTO configuracion = obtenerConfiguracionActiva();

        // Si el subtotal es mayor o igual al monto mínimo, envío gratis
        if (subtotal.compareTo(configuracion.getMontoMinimoEnvioGratis()) >= 0) {
            return BigDecimal.ZERO;
        }

        return configuracion.getCosto();
    }

    /**
     * Crea una nueva configuración de envío
     */
    @Transactional
    public ConfiguracionEnvioResponseDTO crearConfiguracion(ConfiguracionEnvioRequestDTO requestDTO) {
        log.info("Creando nueva configuración de envío: {}", requestDTO.getNombre());

        // Si la nueva configuración está activa, desactivar las anteriores
        if (Boolean.TRUE.equals(requestDTO.getActivo())) {
            desactivarConfiguracionesExistentes();
        }

        ConfiguracionEnvioEntity nuevaConfiguracion = modelMapper.map(requestDTO, ConfiguracionEnvioEntity.class);
        ConfiguracionEnvioEntity configuracionGuardada = configuracionEnvioRepository.save(nuevaConfiguracion);

        log.info("Configuración de envío creada exitosamente: ID {}", configuracionGuardada.getId());
        return modelMapper.map(configuracionGuardada, ConfiguracionEnvioResponseDTO.class);
    }

    /**
     * Actualiza una configuración existente
     */
    @Transactional
    public ConfiguracionEnvioResponseDTO actualizarConfiguracion(Long id, ConfiguracionEnvioRequestDTO requestDTO) {
        log.info("Actualizando configuración de envío ID: {}", id);

        ConfiguracionEnvioEntity configuracionExistente = configuracionEnvioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuración de envío no encontrada: " + id));

        // Si la configuración se está activando, desactivar las demás
        if (Boolean.TRUE.equals(requestDTO.getActivo()) &&
                !configuracionExistente.getActivo().equals(requestDTO.getActivo())) {
            desactivarConfiguracionesExistentes();
        }

        modelMapper.map(requestDTO, configuracionExistente);
        ConfiguracionEnvioEntity configuracionActualizada = configuracionEnvioRepository.save(configuracionExistente);

        log.info("Configuración de envío actualizada exitosamente: ID {}", id);
        return modelMapper.map(configuracionActualizada, ConfiguracionEnvioResponseDTO.class);
    }

    /**
     * Obtiene todas las configuraciones
     */
    public List<ConfiguracionEnvioResponseDTO> obtenerTodasLasConfiguraciones() {
        return configuracionEnvioRepository.findAll().stream()
                .map(config -> modelMapper.map(config, ConfiguracionEnvioResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una configuración específica por ID
     */
    public ConfiguracionEnvioResponseDTO obtenerConfiguracionPorId(Long id) {
        ConfiguracionEnvioEntity configuracion = configuracionEnvioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuración de envío no encontrada: " + id));

        return modelMapper.map(configuracion, ConfiguracionEnvioResponseDTO.class);
    }

    /**
     * Elimina una configuración
     */
    @Transactional
    public void eliminarConfiguracion(Long id) {
        if (!configuracionEnvioRepository.existsById(id)) {
            throw new RuntimeException("Configuración de envío no encontrada: " + id);
        }

        configuracionEnvioRepository.deleteById(id);
        log.info("Configuración de envío eliminada: ID {}", id);
    }

    /**
     * Crea una configuración por defecto
     */
    private ConfiguracionEnvioResponseDTO crearConfiguracionPorDefecto() {
        ConfiguracionEnvioRequestDTO configuracionPorDefecto = new ConfiguracionEnvioRequestDTO();
        configuracionPorDefecto.setNombre("Costo de envío estándar");
        configuracionPorDefecto.setCosto(new BigDecimal("2500.00"));
        configuracionPorDefecto.setMontoMinimoEnvioGratis(new BigDecimal("50000.00"));
        configuracionPorDefecto.setActivo(true);

        ConfiguracionEnvioEntity entity = modelMapper.map(configuracionPorDefecto, ConfiguracionEnvioEntity.class);
        ConfiguracionEnvioEntity guardada = configuracionEnvioRepository.save(entity);

        return modelMapper.map(guardada, ConfiguracionEnvioResponseDTO.class);
    }

    /**
     * Desactiva todas las configuraciones existentes
     */
    private void desactivarConfiguracionesExistentes() {
        List<ConfiguracionEnvioEntity> configuracionesActivas = configuracionEnvioRepository.findByActivoTrue();

        if (!configuracionesActivas.isEmpty()) {
            configuracionesActivas.forEach(config -> config.setActivo(false));
            configuracionEnvioRepository.saveAll(configuracionesActivas);
            log.info("Desactivadas {} configuraciones anteriores", configuracionesActivas.size());
        }
    }
}
