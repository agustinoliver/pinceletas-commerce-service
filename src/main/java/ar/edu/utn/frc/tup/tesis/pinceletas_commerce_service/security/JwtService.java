package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 * Servicio para la gestión de tokens JWT (JSON Web Tokens).
 * Proporciona funcionalidades para generar, validar y extraer información
 * de tokens JWT utilizados en la autenticación de usuarios.
 */

@Service
@Slf4j
public class JwtService {
    /**
     * Genera la clave de firma HMAC-SHA256 a partir de la clave secreta.
     * Convierte la clave secreta definida en SecurityConstants a una clave
     * criptográfica válida para firmar y validar tokens JWT.
     *
     * @return Clave criptográfica para firmar/validar tokens.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SecurityConstants.JWT_SECRET.getBytes());
    }

    /**
     * Genera un token JWT con el email y rol del usuario.
     * El token generado contiene el subject (email), el claim "role",
     * la fecha de emisión y la fecha de expiración.
     *
     * @param email Email del usuario (identificador único).
     * @param role Rol del usuario en el sistema.
     * @return Token JWT firmado en formato String.
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.JWT_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Genera un token JWT con rol por defecto "USER".
     * Sobrecarga del método generateToken para casos donde no se especifica
     * un rol explícito.
     *
     * @param email Email del usuario.
     * @return Token JWT firmado con rol "USER".
     */
    public String generateToken(String email) {
        return generateToken(email, "USER");
    }

    /**
     * Extrae el email (subject) del usuario desde un token JWT.
     *
     * @param token Token JWT del cual extraer el email.
     * @return Email del usuario contenido en el token.
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el rol del usuario desde un token JWT.
     * Si el claim "role" no existe o es null, retorna "USER" como valor por defecto.
     *
     * @param token Token JWT del cual extraer el rol.
     * @return Rol del usuario o "USER" si no está definido.
     */
    public String extractRole(String token) {
        Object role = getClaims(token).get("role");
        return role != null ? role.toString() : "USER";
    }

    /**
     * Valida la integridad y vigencia de un token JWT.
     * Verifica la firma digital, el formato y que el token no haya expirado.
     *
     * @param token Token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la fecha de expiración de un token JWT.
     *
     * @param token Token JWT del cual extraer la fecha de expiración.
     * @return Fecha de expiración del token.
     */
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Parsea y valida un token JWT, extrayendo sus claims.
     * Realiza la validación de la firma digital y extrae el payload del token.
     *
     * @param token Token JWT a parsear.
     * @return Claims contenidos en el token.
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifica si un token JWT ha expirado.
     * Compara la fecha de expiración del token con la fecha actual del sistema.
     *
     * @param token Token JWT a verificar.
     * @return true si el token ha expirado, false si aún es válido.
     */
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }
}
