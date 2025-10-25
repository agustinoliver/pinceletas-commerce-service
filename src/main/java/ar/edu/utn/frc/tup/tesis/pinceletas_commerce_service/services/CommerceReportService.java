package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;


import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports.ProductsByCategoryReport;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports.TopSellingProductsReport;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.CategoriaRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ItemPedidoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para generación de reportes de productos y ventas.
 * Proporciona datos para dashboards administrativos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommerceReportService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    /**
     * Genera un reporte con el conteo de productos por categoría.
     * Incluye productos activos e inactivos.
     *
     * @return Lista de ProductsByCategoryReport con estadísticas por categoría.
     */
    @Cacheable(value = "productsByCategory", unless = "#result.isEmpty()")
    public List<ProductsByCategoryReport> getProductsByCategory() {
        log.info("Generando reporte de productos por categoría");

        List<CategoriaEntity> categorias = categoriaRepository.findAll();
        List<ProductsByCategoryReport> reports = new ArrayList<>();

        for (CategoriaEntity categoria : categorias) {
            long totalProducts = categoria.getProductos() != null ? categoria.getProductos().size() : 0;
            long activeProducts = categoria.getProductos() != null ?
                    categoria.getProductos().stream().filter(p -> p.getActivo()).count() : 0;
            long inactiveProducts = totalProducts - activeProducts;

            ProductsByCategoryReport report = ProductsByCategoryReport.builder()
                    .categoryId(categoria.getId())
                    .categoryName(categoria.getNombre())
                    .totalProducts(totalProducts)
                    .activeProducts(activeProducts)
                    .inactiveProducts(inactiveProducts)
                    .build();

            reports.add(report);

            log.debug("Categoría: {} - Total: {}, Activos: {}, Inactivos: {}",
                    categoria.getNombre(), totalProducts, activeProducts, inactiveProducts);
        }

        log.info("Reporte de productos por categoría generado: {} categorías procesadas", reports.size());
        return reports;
    }

    /**
     * Genera un reporte de los productos más vendidos.
     * Ordenado por cantidad de unidades vendidas (descendente).
     *
     * @param limit Número máximo de productos a retornar (por defecto 10).
     * @return Lista de TopSellingProductsReport con los productos más vendidos.
     */
    @Cacheable(value = "topSellingProducts", key = "#limit", unless = "#result.isEmpty()")
    public List<TopSellingProductsReport> getTopSellingProducts(Integer limit) {
        log.info("Generando reporte de productos más vendidos (top {})", limit);

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // Obtener todos los items de pedidos
        var allItems = itemPedidoRepository.findAll();

        if (allItems.isEmpty()) {
            log.warn("No hay items de pedidos para generar el reporte");
            return new ArrayList<>();
        }

        // Agrupar por producto y calcular estadísticas
        Map<Long, TopSellingProductsReport> productSalesMap = new HashMap<>();

        for (var item : allItems) {
            Long productId = item.getProducto().getId();

            TopSellingProductsReport report = productSalesMap.getOrDefault(productId,
                    TopSellingProductsReport.builder()
                            .productId(productId)
                            .productName(item.getProducto().getNombre())
                            .categoryName(item.getProducto().getCategoria() != null ?
                                    item.getProducto().getCategoria().getNombre() : "Sin categoría")
                            .unitsSold(0L)
                            .totalRevenue(BigDecimal.ZERO)
                            .build()
            );

            // Calcular precio con descuento
            BigDecimal precioConDescuento = item.getPrecioUnitario().subtract(
                    item.getPrecioUnitario().multiply(
                            item.getDescuentoPorcentaje().divide(BigDecimal.valueOf(100))
                    )
            );

            BigDecimal subtotal = precioConDescuento.multiply(BigDecimal.valueOf(item.getCantidad()));

            report.setUnitsSold(report.getUnitsSold() + item.getCantidad());
            report.setTotalRevenue(report.getTotalRevenue().add(subtotal));

            productSalesMap.put(productId, report);
        }

        // Calcular precio promedio y ordenar por unidades vendidas
        List<TopSellingProductsReport> topProducts = productSalesMap.values().stream()
                .peek(report -> {
                    if (report.getUnitsSold() > 0) {
                        BigDecimal avgPrice = report.getTotalRevenue()
                                .divide(BigDecimal.valueOf(report.getUnitsSold()), 2, RoundingMode.HALF_UP);
                        report.setAveragePrice(avgPrice);
                    } else {
                        report.setAveragePrice(BigDecimal.ZERO);
                    }
                })
                .sorted((a, b) -> b.getUnitsSold().compareTo(a.getUnitsSold()))
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Reporte de productos más vendidos generado: {} productos en el top", topProducts.size());
        return topProducts;
    }

    /**
     * Obtiene estadísticas generales de productos.
     *
     * @return Mapa con estadísticas generales (total, activos, inactivos).
     */
    @Cacheable(value = "productGeneralStats")
    public Map<String, Long> getProductGeneralStats() {
        log.info("Generando estadísticas generales de productos");

        var allProducts = productoRepository.findAll();

        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream().filter(p -> p.getActivo()).count();
        long inactiveProducts = totalProducts - activeProducts;

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalProducts", totalProducts);
        stats.put("activeProducts", activeProducts);
        stats.put("inactiveProducts", inactiveProducts);

        log.info("Estadísticas generales - Total: {}, Activos: {}, Inactivos: {}",
                totalProducts, activeProducts, inactiveProducts);

        return stats;
    }
}
