package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemPedidoResponseDTO {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private String imagenProducto;
    private Long opcionSeleccionadaId;
    private String tipoOpcion;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal subtotal;
}
