package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.MercadoPagoResponseDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.PedidoEntity;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {
    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.success-url}")
    private String successUrl;

    @Value("${mercadopago.failure-url}")
    private String failureUrl;

    @Value("${mercadopago.pending-url}")
    private String pendingUrl;

    @Value("${mercadopago.test-mode:false}")
    private boolean testMode;

    @Value("${app.webhook-url}")
    private String webhookUrl;

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            log.info("✅ ========================================");
            log.info("✅ Mercado Pago inicializado correctamente");
            log.info("✅ ========================================");

            String maskedToken = accessToken.substring(0, 20) + "..." +
                    accessToken.substring(accessToken.length() - 10);
            log.info("🔑 Access Token: {}", maskedToken);

            if (testMode) {
                log.warn("⚠️  MODO: PRUEBA (SANDBOX) ⚠️");
                log.warn("⚠️  Los pagos NO son reales");
            } else {
                log.info("🔴 MODO: PRODUCCIÓN 🔴");
                log.info("🔴 Los pagos SON REALES");
            }

            log.info("📢 Webhook URL: {}", webhookUrl);
            log.info("✅ Success URL: {}", successUrl);
            log.info("❌ Failure URL: {}", failureUrl);
            log.info("⏳ Pending URL: {}", pendingUrl);
            log.info("========================================");

            if (!testMode) {
                if (successUrl.contains("localhost") || failureUrl.contains("localhost") || pendingUrl.contains("localhost")) {
                    log.error("❌ ERROR CRÍTICO: No puedes usar localhost en modo PRODUCCIÓN");
                    log.error("❌ Mercado Pago necesita URLs públicas accesibles desde internet");
                    log.error("💡 SOLUCIÓN:");
                    log.error("   1. Levanta ngrok para el frontend: ngrok http 4200");
                    log.error("   2. Copia la URL de ngrok (ej: https://abc123.ngrok-free.app)");
                    log.error("   3. Actualiza application.properties con esa URL");
                    log.error("   4. Reinicia el backend");
                    throw new RuntimeException("URLs de retorno inválidas para producción. Debes usar ngrok o un dominio público.");
                }

                log.info("✅ URLs validadas correctamente para producción");
            }

        } catch (Exception e) {
            log.error("❌ ERROR CRÍTICO configurando Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Error inicializando Mercado Pago", e);
        }
    }

    public MercadoPagoResponseDTO crearPreferenciaPago(PedidoEntity pedido) {
        try {
            log.info("📦 ========================================");
            log.info("📦 Creando preferencia de pago");
            log.info("📦 Pedido: {}", pedido.getNumeroPedido());
            log.info("💰 Total: ${}", pedido.getTotal());
            log.info("🧪 Modo: {}", testMode ? "SANDBOX (Prueba)" : "PRODUCCIÓN (Real)");
            log.info("========================================");

            PreferenceClient client = new PreferenceClient();

            List<PreferenceItemRequest> items = new ArrayList<>();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(pedido.getId().toString())
                    .title("Pedido #" + pedido.getNumeroPedido())
                    .description("Productos de arte y manualidades - Pinceletas")
                    .pictureUrl("https://i.ibb.co/ZMt7LfQ/logo-pinceletas.png")
                    .categoryId("art")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(pedido.getTotal())
                    .build();

            items.add(item);

            log.info("🔗 Construyendo back_urls:");
            log.info("   - Success: {}", successUrl);
            log.info("   - Failure: {}", failureUrl);
            log.info("   - Pending: {}", pendingUrl);

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .installments(12)
                    .defaultInstallments(1)
                    .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(pedido.getEmailContacto())
                    .name(pedido.getEmailContacto().split("@")[0])
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .paymentMethods(paymentMethods)
                    .notificationUrl(webhookUrl)
                    .externalReference(pedido.getNumeroPedido())
                    .statementDescriptor("PINCELETAS")
                    .expires(true)
                    .expirationDateTo(OffsetDateTime.now(ZoneOffset.UTC).plusDays(7))
                    .build();

            log.info("📤 Enviando request a Mercado Pago...");

            if (backUrls.getSuccess() == null || backUrls.getSuccess().isEmpty()) {
                log.error("❌ ERROR: back_urls.success está vacío");
                throw new RuntimeException("La URL de éxito no puede estar vacía");
            }

            Preference preference = client.create(request);

            log.info("✅ ========================================");
            log.info("✅ Preferencia creada exitosamente");
            log.info("🆔 Preference ID: {}", preference.getId());

            String urlPago;
            if (testMode) {
                urlPago = preference.getSandboxInitPoint();
                log.info("🧪 Sandbox Init Point: {}", urlPago);
            } else {
                urlPago = preference.getInitPoint();
                log.info("🔴 Production Init Point: {}", urlPago);
            }

            log.info("🎯 URL de pago seleccionada: {}", urlPago);
            log.info("========================================");

            if (urlPago == null || urlPago.isEmpty()) {
                log.error("❌ ERROR: La URL de pago está vacía");
                log.error("InitPoint: {}", preference.getInitPoint());
                log.error("SandboxInitPoint: {}", preference.getSandboxInitPoint());
                throw new RuntimeException("No se pudo obtener la URL de pago de Mercado Pago");
            }

            return MercadoPagoResponseDTO.builder()
                    .id(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .build();

        } catch (MPApiException e) {
            log.error("❌ ========================================");
            log.error("❌ Error de API Mercado Pago");
            log.error("❌ Status Code: {}", e.getStatusCode());
            log.error("❌ Response: {}", e.getApiResponse().getContent());
            log.error("❌ ========================================");

            String errorMsg = e.getApiResponse().getContent();
            if (errorMsg.contains("back_url.success")) {
                throw new RuntimeException("Error: Las URLs de retorno no están configuradas correctamente. Verifica que success, failure y pending URLs sean válidas y accesibles públicamente.", e);
            }

            throw new RuntimeException("Error al crear preferencia de pago: " + errorMsg, e);

        } catch (MPException e) {
            log.error("❌ ========================================");
            log.error("❌ Error de Mercado Pago SDK");
            log.error("❌ Mensaje: {}", e.getMessage());
            log.error("❌ ========================================");
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("❌ ========================================");
            log.error("❌ Error inesperado");
            log.error("❌ Mensaje: {}", e.getMessage());
            log.error("❌ Tipo: {}", e.getClass().getName());
            log.error("❌ ========================================");
            e.printStackTrace();
            throw new RuntimeException("Error inesperado al crear preferencia de pago", e);
        }
    }
}
