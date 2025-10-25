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

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("✅ Mercado Pago inicializado correctamente");
    }

    public MercadoPagoResponseDTO crearPreferenciaPago(PedidoEntity pedido) {
        try {
            log.info("📦 Creando preferencia de pago para pedido: {}", pedido.getNumeroPedido());

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
                    .success(successUrl)
                    .failure(failureUrl)
                    .pending(pendingUrl)
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(pedido.getNumeroPedido())
                    .notificationUrl(appUrl + "/pedidos/webhook")
                    .binaryMode(true)
                    .build();

            Preference preference = client.create(request);

            log.info("✅ Preferencia de Mercado Pago creada: {}", preference.getId());
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
}
