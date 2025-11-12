package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionEnvioRequestDTO {
    private String nombre;
    private BigDecimal costo;
    private BigDecimal montoMinimoEnvioGratis;
    private Boolean activo;
}
