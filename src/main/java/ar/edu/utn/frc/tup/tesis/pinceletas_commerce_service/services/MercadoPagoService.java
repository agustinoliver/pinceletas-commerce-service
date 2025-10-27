package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.MercadoPagoResponseDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.PedidoEntity;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {
    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Value("${mercadopago.success-url:http://localhost:4200/payment/success}")
    private String successUrl;

    @Value("${mercadopago.failure-url:http://localhost:4200/payment/failure}")
    private String failureUrl;

    @Value("${mercadopago.pending-url:http://localhost:4200/payment/pending}")
    private String pendingUrl;

    // 🆕 NUEVO: Variable para indicar si estamos en modo prueba
    @Value("${mercadopago.test-mode:true}")
    private boolean testMode;

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            // 🔍 Detectar automáticamente si es token de prueba
            boolean isTestToken = accessToken.contains("APP_USR-") || accessToken.contains("TEST");

            log.info("✅ Mercado Pago inicializado correctamente");
            log.info("🔑 Tipo de Access Token: {}", isTestToken ? "PRUEBA (TEST)" : "PRODUCCIÓN");
            log.info("🌐 Modo configurado: {}", testMode ? "PRUEBA" : "PRODUCCIÓN");

            if (isTestToken && !testMode) {
                log.warn("⚠️ ADVERTENCIA: Estás usando un token de PRUEBA pero el modo está en PRODUCCIÓN");
            }

        } catch (Exception e) {
            log.error("❌ Error configurando Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Error inicializando Mercado Pago", e);
        }
    }

    public MercadoPagoResponseDTO crearPreferenciaPago(PedidoEntity pedido) {
        try {
            log.info("📦 Creando preferencia de pago PRUEBA para pedido: {}", pedido.getNumeroPedido());
            log.info("💰 Total del pedido: ${}", pedido.getTotal());

            PreferenceClient client = new PreferenceClient();

            // Crear items de la preferencia
            List<PreferenceItemRequest> items = new ArrayList<>();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(pedido.getId().toString())
                    .title("Pedido #" + pedido.getNumeroPedido() + " - Pinceletas")
                    .description("Productos de arte y manualidades")
                    .pictureUrl("https://via.placeholder.com/300x200/ED620C/FFFFFF?text=Pinceletas")
                    .categoryId("art_supplies")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(pedido.getTotal())
                    .build();

            items.add(item);

            // ✅ URLs de redirección (TODAS son obligatorias)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            // ✅ NUEVO: Crear la preferencia SIN autoReturn ni binaryMode
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(pedido.getNumeroPedido())
                    .notificationUrl(appUrl + "/pedidos/webhook")
                    .statementDescriptor("PINCELETAS")
                    .build();

            Preference preference = client.create(request);

            log.info("✅ Preferencia de Mercado Pago creada: {}", preference.getId());
            log.info("🔗 Init Point (Producción): {}", preference.getInitPoint());
            log.info("🧪 Sandbox Init Point (PRUEBA): {}", preference.getSandboxInitPoint());

            String urlPago = testMode ? preference.getSandboxInitPoint() : preference.getInitPoint();
            log.info("🎯 URL de pago que se usará: {}", urlPago);

            return MercadoPagoResponseDTO.builder()
                    .id(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .build();

        } catch (MPApiException e) {
            log.error("❌ Error de API Mercado Pago: {}", e.getApiResponse().getContent());
            log.error("❌ Status Code: {}", e.getStatusCode());
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getApiResponse().getContent());
        } catch (MPException e) {
            log.error("❌ Error de Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage());
        }
    }
}
