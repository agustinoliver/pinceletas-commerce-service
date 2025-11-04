package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Manejador global de excepciones para toda la aplicación
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Maneja DireccionIncompletaException específicamente
     * Retorna 400 Bad Request con el mensaje de error
     */
    @ExceptionHandler(DireccionIncompletaException.class)
    public ResponseEntity<ErrorResponse> handleDireccionIncompletaException(DireccionIncompletaException ex) {
        log.error("DireccionIncompletaException: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja RuntimeException general (pero con menor prioridad que DireccionIncompletaException)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        // ✅ EXCLUIR EXCEPCIONES DE SWAGGER
        if (isSwaggerRelatedException(ex)) {
            log.debug("Excepción de Swagger ignorada: {}", ex.getMessage());
            return null; // Deja que Spring maneje esta excepción
        }

        log.error("RuntimeException capturada: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones de seguridad (403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException capturada: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "No tienes permisos para realizar esta acción",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja IllegalArgumentException (validaciones)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException capturada: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas no capturadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // ✅ EXCLUIR EXCEPCIONES DE SWAGGER
        if (isSwaggerRelatedException(ex)) {
            log.debug("Excepción de Swagger ignorada: {}", ex.getMessage());
            return null; // Deja que Spring maneje esta excepción
        }

        log.error("Exception genérica capturada: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor. Por favor, contacte al administrador.",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Verifica si la excepción está relacionada con Swagger
     */
    private boolean isSwaggerRelatedException(Exception ex) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }

        // Patrones comunes en excepciones de Swagger
        return message.contains("swagger") ||
                message.contains("openapi") ||
                message.contains("api-docs") ||
                message.contains("swagger-ui") ||
                ex.getClass().getName().contains("swagger") ||
                ex.getClass().getName().contains("openapi");
    }

    /**
     * DTO para respuestas de error consistentes
     */
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
    }
}
