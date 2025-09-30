package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String imagen;
    private Boolean activo;
    private Long categoriaId;

    private List<Long> opcionesIds;
}
