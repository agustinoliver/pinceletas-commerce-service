package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoResponseDTO {
    private Long id;
    private String numeroPedido;
    private Long usuarioId;
    private BigDecimal total;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Datos de envío (snapshot)
    private String direccionEnvio;
    private String ciudadEnvio;
    private String provinciaEnvio;
    private String codigoPostalEnvio;
    private String paisEnvio;

    // Datos de contacto (snapshot)
    private String emailContacto;
    private String telefonoContacto;

    // Datos de Mercado Pago
    private String preferenciaIdMp;
    private String estadoPagoMp;

    private String initPoint;
    private String sandboxInitPoint;

    private List<ItemPedidoResponseDTO> items;
}
