package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionEnvioResponseDTO {

    private Long id;
    private String nombre;
    private BigDecimal costo;
    private BigDecimal montoMinimoEnvioGratis;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
