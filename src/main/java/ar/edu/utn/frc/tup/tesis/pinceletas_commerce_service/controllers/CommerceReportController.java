package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports.ProductsByCategoryReport;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports.TopSellingProductsReport;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.CommerceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para generación de reportes de productos y ventas.
 * Proporciona endpoints para obtener estadísticas y métricas para dashboards.
 *
 * IMPORTANTE: Estos endpoints están diseñados para comunicación entre microservicios
 * y dashboards administrativos. Son de acceso público para facilitar la integración.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commerce Reports", description = "API para reportes y estadísticas de productos y ventas")
public class CommerceReportController {

    private final CommerceReportService reportService;

    /**
     * Obtiene el reporte de productos agrupados por categoría.
     * Muestra la cantidad total, activos e inactivos por cada categoría.
     *
     * @return Lista de ProductsByCategoryReport con estadísticas por categoría.
     */
    @GetMapping("/products/by-category")
    @Operation(
            summary = "Obtener productos por categoría",
            description = "Devuelve estadísticas de productos agrupados por categoría para dashboards"
    )
    public ResponseEntity<List<ProductsByCategoryReport>> getProductsByCategory() {
        log.info("📊 Solicitud de reporte: Productos por categoría");
        List<ProductsByCategoryReport> report = reportService.getProductsByCategory();
        log.info("✅ Reporte generado: {} categorías", report.size());
        return ResponseEntity.ok(report);
    }

    /**
     * Obtiene el reporte de productos más vendidos.
     * Ordenado por cantidad de unidades vendidas (mayor a menor).
     *
     * @param limit Número máximo de productos a retornar (por defecto 10).
     * @return Lista de TopSellingProductsReport con los productos más vendidos.
     */
    @GetMapping("/products/top-selling")
    @Operation(
            summary = "Obtener productos más vendidos",
            description = "Devuelve los productos más vendidos ordenados por unidades vendidas"
    )
    public ResponseEntity<List<TopSellingProductsReport>> getTopSellingProducts(
            @Parameter(description = "Número máximo de productos a retornar")
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("📊 Solicitud de reporte: Top {} productos más vendidos", limit);
        List<TopSellingProductsReport> report = reportService.getTopSellingProducts(limit);
        log.info("✅ Reporte generado: {} productos", report.size());
        return ResponseEntity.ok(report);
    }

    /**
     * Obtiene estadísticas generales de productos.
     *
     * @return Mapa con totales de productos (total, activos, inactivos).
     */
    @GetMapping("/products/general-stats")
    @Operation(
            summary = "Obtener estadísticas generales de productos",
            description = "Devuelve conteos generales de productos (total, activos, inactivos)"
    )
    public ResponseEntity<Map<String, Long>> getProductGeneralStats() {
        log.info("📊 Solicitud de estadísticas generales de productos");
        Map<String, Long> stats = reportService.getProductGeneralStats();
        log.info("✅ Estadísticas generadas: {} métricas", stats.size());
        return ResponseEntity.ok(stats);
    }
}
