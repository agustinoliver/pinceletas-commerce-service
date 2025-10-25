package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.configs;

import com.mercadopago.MercadoPagoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class MercadoPagoConfiguration {
    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            System.out.println("✅ Mercado Pago configurado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error configurando Mercado Pago: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
