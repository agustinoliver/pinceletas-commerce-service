package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para el reporte de compras agrupadas por usuario.
 * Utilizado para dashboards y reportes administrativos.
 *
 * Muestra información agregada de las compras realizadas por cada usuario,
 * incluyendo totales, cantidades y última actividad de compra.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchasesByUserReport {

    /** ID del usuario en el sistema */
    private Long userId;

    /** Nombre completo del usuario */
    private String userName;

    /** Email del usuario */
    private String userEmail;

    /** Cantidad total de pedidos realizados por el usuario */
    private Long totalPurchases;

    /** Monto total gastado por el usuario (suma de todos sus pedidos) */
    private BigDecimal totalAmountSpent;

    /** Monto promedio por pedido */
    private BigDecimal averageOrderAmount;

    /** Fecha y hora de la última compra realizada */
    private LocalDateTime lastPurchaseDate;

    /** Estado del último pedido realizado */
    private String lastOrderStatus;

    /** Número del último pedido realizado */
    private String lastOrderNumber;
}
