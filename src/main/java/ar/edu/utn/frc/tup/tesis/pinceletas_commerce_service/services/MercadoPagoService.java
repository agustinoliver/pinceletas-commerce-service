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

    // ✅ NUEVO: Inicializar el SDK de Mercado Pago
    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("✅ Mercado Pago inicializado correctamente");
        log.info("   - Access Token: {}...", accessToken.substring(0, 20));
    }

    public MercadoPagoResponseDTO crearPreferenciaPago(PedidoEntity pedido) {
        try {
            log.info("📦 Creando preferencia de pago:");
            log.info("   - Pedido: {}", pedido.getNumeroPedido());
            log.info("   - Total: {}", pedido.getTotal());
            log.info("   - Success URL: {}", successUrl + "?pedido=" + pedido.getNumeroPedido());
            log.info("   - Failure URL: {}", failureUrl + "?pedido=" + pedido.getNumeroPedido());
            log.info("   - Pending URL: {}", pendingUrl + "?pedido=" + pedido.getNumeroPedido());
            log.info("   - Webhook URL: {}", appUrl + "/pedidos/webhook");

            PreferenceClient client = new PreferenceClient();

            // Crear items de la preferencia
            List<PreferenceItemRequest> items = new ArrayList<>();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(pedido.getId().toString())
                    .title("Pedido #" + pedido.getNumeroPedido())
                    .description("Productos de Pinceletas")
                    .pictureUrl("https://www.pinceletas.com/logo.png")
                    .categoryId("art")
                    .quantity(1)
                    .currencyId("ARS")
                    .unitPrice(pedido.getTotal())
                    .build();

            items.add(item);

            // URLs de redirección
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl + "?pedido=" + pedido.getNumeroPedido())
                    .failure(failureUrl + "?pedido=" + pedido.getNumeroPedido())
                    .pending(pendingUrl + "?pedido=" + pedido.getNumeroPedido())
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(pedido.getNumeroPedido())
                    .notificationUrl(appUrl + "/pedidos/webhook")
                    .build();

            Preference preference = client.create(request);

            log.info("✅ Preferencia de Mercado Pago creada: {}", preference.getId());
            log.info("✅ Init Point: {}", preference.getInitPoint());
            log.info("✅ Sandbox Init Point: {}", preference.getSandboxInitPoint());

            return MercadoPagoResponseDTO.builder()
                    .id(preference.getId())
                    .initPoint(preference.getInitPoint())
                    .sandboxInitPoint(preference.getSandboxInitPoint())
                    .build();

        } catch (MPApiException e) {
            log.error("❌ Error de API Mercado Pago: {}", e.getApiResponse().getContent());
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getApiResponse().getContent());
        } catch (MPException e) {
            log.error("❌ Error de Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage());
        }
    }

    public void procesarPago(String paymentId) {
        try {
            log.info("Procesando pago con ID: {}", paymentId);
        } catch (Exception e) {
            log.error("Error al procesar pago: {}", e.getMessage());
            throw new RuntimeException("Error al procesar pago");
        }
    }
}