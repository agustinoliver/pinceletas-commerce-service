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
    public ResponseEntity<String> procesarWebhook(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) String rawBody) {

        try {
            log.info("🔔 ========== WEBHOOK RECIBIDO ==========");
            log.info("📋 Topic: {}", topic);
            log.info("📋 ID (resource): {}", id);
            log.info("📋 Raw Body: {}", rawBody);
            log.info("========================================");

            // Mercado Pago puede enviar el webhook de varias formas
            // Intentar procesar con la información disponible

            if ("payment".equals(topic) && id != null) {
                log.info("🔄 Procesando notificación de pago: {}", id);

                // Consultar el pago usando el ID
                pedidoService.procesarNotificacionMercadoPago(id);

                log.info("✅ Webhook de pago procesado exitosamente");
            }
            else if ("merchant_order".equals(topic) && id != null) {
                log.info("🔄 Procesando notificación de orden: {}", id);
                // Aquí podrías manejar merchant_order si lo necesitas
                log.info("⚠️ Merchant order recibido pero no procesado");
            }
            else {
                log.warn("⚠️ Webhook recibido con topic desconocido o sin ID");
                log.warn("⚠️ Topic: {}, ID: {}", topic, id);
            }
            return ResponseEntity.ok("OK");


        } catch (Exception e) {
            log.error("❌ Error procesando webhook", e);
            // Aún con error, retornar OK para evitar reintentos
            return ResponseEntity.ok("ERROR");
        }
    }
    // Endpoint para testing
    @GetMapping("/webhook/test")
    public ResponseEntity<Map<String, String>> testWebhook() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Webhook endpoint is working");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("url", "https://pinceletas-commerce-service.onrender.com/pedidos/webhook");
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
