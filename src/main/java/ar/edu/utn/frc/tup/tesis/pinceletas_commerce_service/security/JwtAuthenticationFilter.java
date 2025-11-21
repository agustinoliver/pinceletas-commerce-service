package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT para Spring Security.
 * Intercepta todas las peticiones HTTP y valida el token JWT presente
 * en el header Authorization. Si el token es válido, establece la autenticación
 * en el contexto de seguridad de Spring.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    /** Servicio para operaciones de validación y extracción de datos del JWT. */
    private final JwtService jwtService;

    /**
     * Método principal del filtro que procesa cada petición HTTP.
     * Extrae el token del header, lo valida, extrae la información del usuario
     * y establece la autenticación en el contexto de seguridad.
     *
     * @param request Petición HTTP entrante.
     * @param response Respuesta HTTP.
     * @param filterChain Cadena de filtros de Spring Security.
     * @throws ServletException Si ocurre un error en el procesamiento del servlet.
     * @throws IOException Si ocurre un error de entrada/salida.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.debug("Procesando request: {} {}", request.getMethod(), requestPath);

        final String token = getTokenFromRequest(request);

        if (token == null) {
            log.debug("No se encontró token JWT para: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtService.isTokenValid(token)) {
                String email = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);

                log.debug("Token válido para usuario: {} con rol: {}", email, role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority("ROLE_" + role)
                                )
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Usuario autenticado exitosamente: {}", email);
            } else {
                log.warn("Token inválido para request: {}", requestPath);
            }
        } catch (Exception e) {
            log.error("Error procesando JWT para {}: {}", requestPath, e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization de la petición HTTP.
     * Busca el header "Authorization" y verifica que comience con el prefijo "Bearer ".
     * Si existe y tiene el formato correcto, retorna el token sin el prefijo.
     *
     * @param request Petición HTTP de la cual extraer el token.
     * @return El token JWT sin el prefijo "Bearer ", o null si no existe o es inválido.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(SecurityConstants.JWT_HEADER);

        if (StringUtils.hasText(authHeader) &&
                authHeader.startsWith(SecurityConstants.JWT_PREFIX)) {
            return authHeader.substring(7); // Remover "Bearer "
        }

        return null;
    }

    /**
     * Define las rutas que NO deben pasar por este filtro de autenticación.
     * Las rutas públicas definidas aquí no requerirán un token JWT válido.
     * Este método puede ser sobrescrito en cada microservicio para agregar
     * rutas específicas según sus necesidades.
     *
     * @param request Petición HTTP a evaluar.
     * @return true si la ruta NO debe ser filtrada (es pública), false si debe pasar por el filtro.
     * @throws ServletException Si ocurre un error en el procesamiento.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/h2-console/") ||
                path.equals("/health") ||
                path.equals("/actuator/health");
    }
}
