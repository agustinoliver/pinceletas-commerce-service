package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para el reporte de pedidos por estado.
 * Muestra estadísticas de pedidos agrupados por estado.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdersByStatusReport {

    /** Estado del pedido (PENDIENTE, PAGADO, PROCESANDO, etc.) */
    private String status;

    /** Cantidad total de pedidos en este estado */
    private Long totalOrders;

    /** Ingresos totales de pedidos en este estado */
    private BigDecimal totalRevenue;

    /** Porcentaje del total de pedidos */
    private Double percentage;
}
