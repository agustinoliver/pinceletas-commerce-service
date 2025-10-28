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

    @Value("${mercadopago.success-url:http://localhost:4200/payment/success}")
    private String successUrl;

    @Value("${mercadopago.failure-url:http://localhost:4200/payment/failure}")
    private String failureUrl;

    @Value("${mercadopago.pending-url:http://localhost:4200/payment/pending}")
    private String pendingUrl;

    @Value("${mercadopago.test-mode:true}")
    private boolean testMode;

    @Value("${app.webhook-url}")
    private String webhookUrl;

    @PostConstruct
    public void init() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            boolean isTestToken = accessToken.contains("APP_USR-") && accessToken.contains("TEST");

            log.info("✅ Mercado Pago inicializado correctamente");
            log.info("🔑 Tipo de Access Token: {}", isTestToken ? "PRUEBA (TEST)" : "PRODUCCIÓN");
            log.info("🌐 Modo configurado: {}", testMode ? "PRUEBA" : "PRODUCCIÓN");
            log.info("🔔 Webhook URL: {}", webhookUrl);
            log.info("✅ Success URL: {}", successUrl);
            log.info("❌ Failure URL: {}", failureUrl);
            log.info("⏳ Pending URL: {}", pendingUrl);

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
            log.info("📦 Creando preferencia de pago para pedido: {}", pedido.getNumeroPedido());
            log.info("💰 Total del pedido: ${}", pedido.getTotal());

            PreferenceClient client = new PreferenceClient();

            // ✅ ITEM DE LA PREFERENCIA
            List<PreferenceItemRequest> items = new ArrayList<>();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(pedido.getId().toString())
                    .title("Pedido #" + pedido.getNumeroPedido())
                    .description("Productos de arte y manualidades - Pinceletas")
                    .pictureUrl("https://via.placeholder.com/300x200/ED620C/FFFFFF?text=Pinceletas")
                    .categoryId("art")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(pedido.getTotal())
                    .build();

            items.add(item);

            // ✅ URLs DE RETORNO (OBLIGATORIAS - TODAS DEBEN ESTAR DEFINIDAS)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            log.info("🔗 Back URLs configuradas:");
            log.info("   Success: {}", successUrl);
            log.info("   Failure: {}", failureUrl);
            log.info("   Pending: {}", pendingUrl);

            // ✅ CONFIGURACIÓN DE MÉTODOS DE PAGO
            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .installments(12) // Máximo 12 cuotas
                    .defaultInstallments(1) // Por defecto 1 cuota
                    .build();

            // ✅ INFORMACIÓN DEL PAGADOR (OPCIONAL PERO RECOMENDADO)
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .email(pedido.getEmailContacto())
                    .name(pedido.getEmailContacto().split("@")[0])
                    .build();

            // ✅ CREAR LA PREFERENCIA SIN autoReturn
            // IMPORTANTE: NO usar autoReturn para evitar el error
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .payer(payer)
                    .backUrls(backUrls)
                    // ❌ NO USAR: .autoReturn("approved") - Esto causa el error
                    .paymentMethods(paymentMethods)
                    .notificationUrl(webhookUrl)
                    .externalReference(pedido.getNumeroPedido())
                    .statementDescriptor("PINCELETAS")
                    .expires(true) // La preferencia expirará
                    .expirationDateTo(OffsetDateTime.now(ZoneOffset.UTC).plusDays(7)) // Expira en 7 días
                    .build();

            log.info("📤 Enviando request a Mercado Pago...");

            // ✅ CREAR LA PREFERENCIA
            Preference preference = client.create(request);

            log.info("✅ Preferencia de Mercado Pago creada exitosamente");
            log.info("🆔 Preference ID: {}", preference.getId());
            log.info("🔗 Init Point (Producción): {}", preference.getInitPoint());
            log.info("🧪 Sandbox Init Point (Prueba): {}", preference.getSandboxInitPoint());

            // ✅ DETERMINAR QUÉ URL USAR SEGÚN EL MODO
            String urlPago = testMode ? preference.getSandboxInitPoint() : preference.getInitPoint();
            log.info("🎯 URL de pago seleccionada ({}): {}", testMode ? "SANDBOX" : "PRODUCCIÓN", urlPago);

            if (urlPago == null || urlPago.isEmpty()) {
                log.error("❌ ERROR: La URL de pago está vacía");
                throw new RuntimeException("No se pudo obtener la URL de pago de Mercado Pago");
            }

            return MercadoPagoResponseDTO.builder()
                    .id(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .build();

        } catch (MPApiException e) {
            log.error("❌ Error de API Mercado Pago");
            log.error("Status Code: {}", e.getStatusCode());
            log.error("Response: {}", e.getApiResponse().getContent());
            log.error("Causa: {}", e.getCause());
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getApiResponse().getContent(), e);

        } catch (MPException e) {
            log.error("❌ Error de Mercado Pago SDK");
            log.error("Mensaje: {}", e.getMessage());
            log.error("Causa: {}", e.getCause());
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("❌ Error inesperado al crear preferencia");
            log.error("Mensaje: {}", e.getMessage());
            log.error("Tipo: {}", e.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("Error inesperado al crear preferencia de pago", e);
        }
    }
}
