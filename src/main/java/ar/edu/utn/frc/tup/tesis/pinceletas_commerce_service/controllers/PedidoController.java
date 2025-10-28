package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.*;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaPedidoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.JwtExtractorService;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {
    private final PedidoService pedidoService;
    private final JwtExtractorService jwtExtractorService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@RequestBody PedidoRequestDTO pedidoRequest) {
        String authToken = jwtExtractorService.extractToken();
        if (authToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PedidoResponseDTO pedido = pedidoService.crearPedido(pedidoRequest, authToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPedidosPorUsuario(@PathVariable Long usuarioId) {
        String authToken = jwtExtractorService.extractToken();
        if (authToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<PedidoResponseDTO> pedidos = pedidoService.obtenerPedidosPorUsuario(usuarioId, authToken);
        return ResponseEntity.ok(pedidos);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodosLosPedidos() {
        String authToken = jwtExtractorService.extractToken();
        if (authToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<PedidoResponseDTO> pedidos = pedidoService.obtenerTodosLosPedidos(authToken);
        return ResponseEntity.ok(pedidos);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedidoPorId(@PathVariable Long id) {
        String authToken = jwtExtractorService.extractToken();
        if (authToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PedidoResponseDTO pedido = pedidoService.obtenerPedidoPorId(id, authToken);
        return ResponseEntity.ok(pedido);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/numero/{numeroPedido}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedidoPorNumero(@PathVariable String numeroPedido) {
        String authToken = jwtExtractorService.extractToken();
        if (authToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PedidoResponseDTO pedido = pedidoService.obtenerPedidoPorNumero(numeroPedido, authToken);
        return ResponseEntity.ok(pedido);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> actualizarEstadoPedido(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoPedidoDTO actualizarEstadoDTO) {
        String authToken = jwtExtractorService.extractToken();
        if (authToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PedidoResponseDTO pedido = pedidoService.actualizarEstadoPedido(id, actualizarEstadoDTO, authToken);
        return ResponseEntity.ok(pedido);
    }

    /**
     * ✅ WEBHOOK CORREGIDO - Recibe notificaciones de Mercado Pago
     * Mercado Pago envía: topic, id (resource), y opcionalmente otros parámetros
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> procesarWebhook(
            @RequestParam(required = false) String preference_id,
            @RequestParam(required = false) String payment_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String external_reference,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String type,
            @RequestBody(required = false) Map<String, Object> payload) {

        try {
            log.info("🔔 ========== WEBHOOK RECIBIDO ==========");
            log.info("📋 Parámetros recibidos:");
            log.info("  - preference_id: {}", preference_id);
            log.info("  - payment_id: {}", payment_id);
            log.info("  - status: {}", status);
            log.info("  - external_reference: {}", external_reference);
            log.info("  - topic: {}", topic);
            log.info("  - type: {}", type);
            log.info("  - payload: {}", payload);
            log.info("========================================");

            // Mercado Pago puede enviar el webhook de varias formas
            // Intentar procesar con la información disponible

            if (preference_id != null && !preference_id.isEmpty()) {
                log.info("🔄 Procesando webhook con preference_id: {}", preference_id);
                pedidoService.procesarWebhook(preference_id, payment_id, status);
            }
            else if (external_reference != null && !external_reference.isEmpty()) {
                log.info("🔄 Procesando webhook con external_reference: {}", external_reference);
                // Si tienes external_reference, puedes buscar por ID
                // pedidoService.procesarWebhookPorId(Long.parseLong(external_reference), payment_id, status);
                log.warn("⚠️ Procesamiento por external_reference no implementado aún");
            }
            else {
                log.warn("⚠️ Webhook recibido sin preference_id ni external_reference");
                log.warn("⚠️ Payload completo: {}", payload);
            }

            // IMPORTANTE: Siempre retornar 200 OK para que Mercado Pago no reintente
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ Error procesando webhook", e);
            log.error("❌ Stack trace:", e);

            // Aún con error, retornar OK para evitar reintentos infinitos
            return ResponseEntity.ok().build();
        }
    }
    // ============================================
    // ENDPOINT ADICIONAL PARA DEBUGGING (OPCIONAL)
    // Te ayuda a ver qué datos está enviando Mercado Pago
    // ============================================
    @GetMapping("/webhook/test")
    public ResponseEntity<Map<String, String>> testWebhook() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Webhook endpoint is working");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/auditoria")
    public ResponseEntity<List<AuditoriaPedidoEntity>> obtenerAuditoriaPedidos() {
        return ResponseEntity.ok(pedidoService.consultarAuditoriaPedidos());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{pedidoId}/auditoria")
    public ResponseEntity<List<AuditoriaPedidoEntity>> obtenerAuditoriaPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoService.consultarAuditoriaPorPedido(pedidoId));
    }


}
