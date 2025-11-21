package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.configs;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ========================================
                        // 1. ENDPOINTS COMPLETAMENTE PÚBLICOS
                        // ========================================

                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        // Health checks
                        .requestMatchers("/health", "/actuator/health").permitAll()

                        // Imágenes (acceso público)
                        .requestMatchers("/uploads/**").permitAll()

                        // ✅ PRODUCTOS Y CATEGORÍAS - Solo GET público
                        .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()

                        // Webhook de Mercado Pago (debe estar ANTES de /pedidos)
                        .requestMatchers(HttpMethod.POST, "/pedidos/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pedidos/webhook/test").permitAll()

                        // Reportes para comunicación entre microservicios
                        .requestMatchers(HttpMethod.GET, "/api/reports/**").permitAll()

                        // ========================================
                        // 2. PRODUCTOS - Operaciones protegidas
                        // ========================================
                        .requestMatchers(HttpMethod.POST, "/productos/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/productos/**").hasRole("ADMIN")

                        // ========================================
                        // 3. CATEGORÍAS - Operaciones protegidas
                        // ========================================
                        .requestMatchers(HttpMethod.POST, "/categorias/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasRole("ADMIN")

                        // ========================================
                        // 4. PEDIDOS - Rutas específicas primero
                        // ========================================
                        .requestMatchers(HttpMethod.GET, "/pedidos/usuario/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/numero/**").hasAnyRole("USER", "ADMIN")

                        // Pedidos generales
                        .requestMatchers(HttpMethod.POST, "/pedidos").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/pedidos/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/pedidos/**").hasRole("ADMIN")

                        // ========================================
                        // 5. OPCIONES DE PRODUCTOS
                        // ========================================
                        .requestMatchers(HttpMethod.GET, "/opciones-productos/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/opciones-productos/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/opciones-productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/opciones-productos/**").hasRole("ADMIN")

                        // ========================================
                        // 6. FAVORITOS Y CARRITO (requieren autenticación)
                        // ========================================
                        .requestMatchers("/favoritos/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/carrito/**").hasAnyRole("USER", "ADMIN")

                        // ========================================
                        // 7. CUALQUIER OTRA PETICIÓN
                        // ========================================
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("➡️ CorsConfigurationSource bean initialized");
        System.out.println("➡️ Frontend URL: " + frontendUrl);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "https://interior-visiting-levy-expectations.trycloudflare.com",
                "https://say-sys-next-embassy.trycloudflare.com",
                "https://*.trycloudflare.com",
                "https://pinceletas-commerce-service.onrender.com",
                "https://pinceletas-frontend.onrender.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}