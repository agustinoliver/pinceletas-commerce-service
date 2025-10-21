package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedidos")
public class PedidoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroPedido;

    @Column(nullable = false)
    private Long usuarioId; // ID del usuario en user-auth-service

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @Column(nullable = false)
    private String tipoEntrega;

    // Datos de envío (snapshot al momento del pedido)
    private String direccionEnvio;
    private String ciudadEnvio;
    private String provinciaEnvio;
    private String codigoPostalEnvio;
    private String paisEnvio;

    // Datos de contacto (snapshot al momento del pedido)
    private String emailContacto;
    private String telefonoContacto;

    // Datos de Mercado Pago
    private String preferenciaIdMp;
    private String pagoIdMp;
    private String estadoPagoMp;
    private LocalDateTime fechaPagoMp;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedidoEntity> items;

    // Método helper para obtener información resumida del pedido
    public String getResumenEstado() {
        return String.format("Pedido %s - %s - $%.2f",
                numeroPedido, estado, total);
    }

    // Método para verificar si el pedido puede ser modificado
    public boolean puedeSerModificado() {
        return estado == EstadoPedido.PENDIENTE ||
                estado == EstadoPedido.PENDIENTE_PAGO ||
                estado == EstadoPedido.PAGADO;
    }

    // Método para verificar si el pedido está completado
    public boolean estaCompletado() {
        return estado == EstadoPedido.ENTREGADO;
    }

    // Método para verificar si el pedido está cancelado
    public boolean estaCancelado() {
        return estado == EstadoPedido.CANCELADO ||
                estado == EstadoPedido.REEMBOLSADO;
    }
}
