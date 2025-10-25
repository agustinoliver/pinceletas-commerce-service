package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para el reporte de productos más vendidos.
 * Utilizado para dashboards y reportes administrativos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopSellingProductsReport {

    /** ID del producto */
    private Long productId;

    /** Nombre del producto */
    private String productName;

    /** Nombre de la categoría */
    private String categoryName;

    /** Total de unidades vendidas */
    private Long unitsSold;

    /** Ingresos totales generados por este producto */
    private BigDecimal totalRevenue;

    /** Precio promedio de venta */
    private BigDecimal averagePrice;
}
