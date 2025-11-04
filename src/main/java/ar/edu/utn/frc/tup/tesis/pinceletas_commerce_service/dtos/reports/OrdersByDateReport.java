package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para el reporte de pedidos por fecha.
 * Muestra estadísticas de pedidos agrupados por fecha.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdersByDateReport {

    /** Fecha del pedido */
    private LocalDate date;

    /** Cantidad total de pedidos en esa fecha */
    private Long totalOrders;

    /** Ingresos totales generados en esa fecha */
    private BigDecimal totalRevenue;

    /** Cantidad de pedidos completados */
    private Long completedOrders;

    /** Cantidad de pedidos cancelados */
    private Long cancelledOrders;

    /** Cantidad de pedidos pendientes */
    private Long pendingOrders;
}
