package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.security;

/**
 * Clase de utilidad que centraliza las constantes de seguridad JWT.
 * Define los parámetros de configuración necesarios para la generación y
 * validación de tokens JWT que deben ser consistentes entre todos los
 * microservicios del sistema Pinceletas.
 */
public class SecurityConstants {
    /**
     * Clave secreta para firmar y validar tokens JWT.
     * Esta clave debe ser la misma en TODOS los microservicios.
     * ⚠️ EN PRODUCCIÓN: Usar variables de entorno o sistemas de gestión de secretos.
     */
    public static final String JWT_SECRET = "supersecretkeysupersecretkeysupersecretkey";
    /** Tiempo de expiración del token en milisegundos (1 hora). */
    public static final long JWT_EXPIRATION = 1000 * 60 * 60;
    /** Nombre del header HTTP donde se envía el token JWT. */
    public static final String JWT_HEADER = "Authorization";
    /** Prefijo del token en el header Authorization. */
    public static final String JWT_PREFIX = "Bearer ";
    /**
     * Constructor privado para evitar instanciación de la clase de utilidad.
     */
    private SecurityConstants() {
        throw new IllegalStateException("Utility class");
    }
}
