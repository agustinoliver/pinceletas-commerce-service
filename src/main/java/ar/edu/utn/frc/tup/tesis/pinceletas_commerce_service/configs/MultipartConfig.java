package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.configs;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Configurar tamaños máximos
        factory.setMaxFileSize(DataSize.ofMegabytes(10)); // 10MB por archivo
        factory.setMaxRequestSize(DataSize.ofMegabytes(15)); // 15MB por request total

        return factory.createMultipartConfig();
    }
}
