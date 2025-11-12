package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaPedidoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.PedidoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.AccionAuditoria;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.EstadoPedido;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.AuditoriaPedidoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoCleanupScheduler {

    private final PedidoRepository pedidoRepository;
    private final AuditoriaPedidoRepository auditoriaPedidoRepository;
    private final ObjectMapper objectMapper;

    /**
     * Ejecuta cada día a las 2:00 AM
     * Elimina pedidos PENDIENTE_PAGO con más de 7 días
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void limpiarPedidosAbandonados() {
        log.info("🧹 ========== INICIO LIMPIEZA AUTOMÁTICA DE PEDIDOS ==========");

        LocalDateTime limite = LocalDateTime.now().minusDays(7);

        List<PedidoEntity> pedidosAEliminar = pedidoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE_PAGO)
                .filter(p -> p.getFechaCreacion().isBefore(limite))
                .toList();

        if (pedidosAEliminar.isEmpty()) {
            log.info("✅ No hay pedidos para eliminar");
            log.info("========================================");
            return;
        }

        log.info("🗑️ Pedidos a eliminar: {}", pedidosAEliminar.size());

        int eliminados = 0;
        for (PedidoEntity pedido : pedidosAEliminar) {
            try {
                registrarAuditoriaEliminacion(pedido);
                pedidoRepository.delete(pedido);
                eliminados++;

                log.info("✅ Eliminado: {} (Creado: {})",
                        pedido.getNumeroPedido(),
                        pedido.getFechaCreacion());
            } catch (Exception e) {
                log.error("❌ Error eliminando pedido {}: {}",
                        pedido.getNumeroPedido(), e.getMessage());
            }
        }

        log.info("✅ Limpieza completada: {} pedidos eliminados", eliminados);
        log.info("========================================");
    }

    private void registrarAuditoriaEliminacion(PedidoEntity pedido) {
        AuditoriaPedidoEntity auditoria = new AuditoriaPedidoEntity();
        auditoria.setPedidoId(pedido.getId());
        auditoria.setAccion(AccionAuditoria.ELIMINAR);
        auditoria.setUsuarioId(0L); // Sistema automático
        auditoria.setFechaAccion(LocalDateTime.now());
        auditoria.setValoresAnteriores(serializarPedido(pedido));
        auditoria.setValoresNuevos("{ \"eliminadoPor\": \"SISTEMA_AUTOMATICO\", \"razon\": \"7_DIAS_PENDIENTE_PAGO\" }");

        auditoriaPedidoRepository.save(auditoria);
    }

    private String serializarPedido(PedidoEntity pedido) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", pedido.getId());
            data.put("numeroPedido", pedido.getNumeroPedido());
            data.put("usuarioId", pedido.getUsuarioId());
            data.put("total", pedido.getTotal());
            data.put("estado", pedido.getEstado());
            data.put("fechaCreacion", pedido.getFechaCreacion());
            data.put("emailContacto", pedido.getEmailContacto());
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}
