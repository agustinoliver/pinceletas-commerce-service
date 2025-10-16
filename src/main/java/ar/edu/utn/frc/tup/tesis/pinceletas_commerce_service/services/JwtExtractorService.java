package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class JwtExtractorService {
    private final HttpServletRequest request;

    public String extractToken() {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String extractEmailFromToken() {
        // En una implementación real, decodificarías el JWT para extraer el email
        // Por ahora, necesitamos que el frontend envíe el email del usuario
        throw new UnsupportedOperationException("Extraer email del token no implementado");
    }
}
