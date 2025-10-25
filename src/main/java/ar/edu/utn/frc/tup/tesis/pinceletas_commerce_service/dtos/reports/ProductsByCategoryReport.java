package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el reporte de productos por categoría.
 * Utilizado para dashboards y reportes administrativos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductsByCategoryReport {

    /** Nombre de la categoría */
    private String categoryName;

    /** ID de la categoría */
    private Long categoryId;

    /** Total de productos en esta categoría */
    private Long totalProducts;

    /** Total de productos activos en esta categoría */
    private Long activeProducts;

    /** Total de productos inactivos en esta categoría */
    private Long inactiveProducts;
}
