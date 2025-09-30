package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.uploads.path}")
    private String uploadsPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsPath);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new Converter<String, List<Long>>() {
            @Override
            public List<Long> convert(String source) {
                if (source == null || source.trim().isEmpty()) {
                    return null;
                }
                return Arrays.stream(source.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
            }
        });
    }
}