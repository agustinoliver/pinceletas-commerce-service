package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActualizarEstadoPedidoDTO {
    private EstadoPedido estado;
}
