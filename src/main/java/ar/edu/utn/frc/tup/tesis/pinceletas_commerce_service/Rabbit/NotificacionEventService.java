package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.Rabbit;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.Rabbit.events.NotificacionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificacionEventService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEventService.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:notificaciones.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key:notificaciones.key}")
    private String routingKey;

    public NotificacionEventService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Notificación para ADMIN cuando un USER hace un pedido
     */
    public void enviarNotificacionNuevoPedido(String emailUsuario, String numeroPedido, String detallesPedido, Long usuarioId, String nombreUsuario) {
        log.info("🔔 Preparando notificación de NUEVO PEDIDO para ADMIN");

        NotificacionEvent evento = new NotificacionEvent(
                "🛍️ Nuevo pedido realizado",
                "El usuario " + nombreUsuario + " (" + emailUsuario + ") ha realizado el pedido #" + numeroPedido + ". " + detallesPedido,
                "NUEVO_PEDIDO",
                null, // usuarioId null para ADMIN (se resuelve en notification-service)
                "{\"email\": \"" + emailUsuario + "\", \"numeroPedido\": \"" + numeroPedido + "\", \"usuarioId\": " + usuarioId + ", \"tipo\": \"nuevo_pedido\"}",
                "ADMIN"
        );

        enviarEvento(evento);
        log.info("✅ Notificación de NUEVO PEDIDO enviada para ADMIN - pedido: {}", numeroPedido);
    }

    /**
     * Notificación para USER cuando su pedido cambia de estado
     */
    public void enviarNotificacionEstadoPedido(String emailUsuario, Long usuarioId, String numeroPedido, String nuevoEstado, String nombreUsuario) {
        log.info("🔔 Preparando notificación de CAMBIO DE ESTADO para USUARIO: {}", emailUsuario);

        String mensajeEstado = obtenerMensajeSegunEstado(nuevoEstado);

        NotificacionEvent evento = new NotificacionEvent(
                "📦 Actualización de pedido #" + numeroPedido,
                "Hola " + nombreUsuario + ", " + mensajeEstado,
                "ESTADO_PEDIDO",
                usuarioId, // Para el USER específico
                "{\"email\": \"" + emailUsuario + "\", \"numeroPedido\": \"" + numeroPedido + "\", \"estado\": \"" + nuevoEstado + "\", \"tipo\": \"estado_pedido\"}",
                "USER"
        );

        enviarEvento(evento);
        log.info("✅ Notificación de CAMBIO DE ESTADO enviada para USUARIO: {} - pedido: {}", emailUsuario, numeroPedido);
    }

    private String obtenerMensajeSegunEstado(String estado) {
        return switch (estado) {
            case "PAGADO" -> "tu pedido ha sido confirmado y está siendo procesado. ¡Gracias por tu compra! 🎉";
            case "PROCESANDO" -> "estamos preparando tu pedido. 📦";
            case "ENVIADO" -> "tu pedido está en camino. ¡Pronto lo recibirás! 🚚";
            case "ENTREGADO" -> "tu pedido ha sido entregado. ¡Esperamos que lo disfrutes! ✅";
            case "CANCELADO" -> "tu pedido ha sido cancelado. Si tienes dudas, contáctanos. ❌";
            case "REEMBOLSADO" -> "tu pedido ha sido reembolsado. El dinero estará disponible pronto. 💰";
            default -> "el estado de tu pedido ha cambiado a: " + estado + ".";
        };
    }

    private void enviarEvento(NotificacionEvent evento) {
        try {
            log.debug("📤 Enviando a exchange: {}, routing-key: {}", exchange, routingKey);
            log.debug("📦 Contenido del evento: {}", evento);

            rabbitTemplate.convertAndSend(exchange, routingKey, evento);

            log.info("✅ Evento enviado exitosamente a RabbitMQ");
        } catch (Exception e) {
            log.error("❌ Error enviando evento de notificación: {}", e.getMessage(), e);
        }
    }
}
