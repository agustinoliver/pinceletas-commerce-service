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

    @PostMapping("/webhook")
    public ResponseEntity<Void> procesarWebhook(
            @RequestParam String preference_id,
            @RequestParam String payment_id,
            @RequestParam String status) {

        try {
            log.info("Webhook recibido - Preferencia: {}, Estado: {}", preference_id, status);
            pedidoService.procesarWebhook(preference_id, payment_id, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error procesando webhook", e);
            // Retornar OK aunque sea error para que MP no reintente
            return ResponseEntity.ok().build();
        }
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
