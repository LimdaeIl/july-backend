package com.backend.july.payment.infrastructure.toss;

import com.backend.july.payment.infrastructure.toss.dto.TossCancelRequest;
import com.backend.july.payment.infrastructure.toss.dto.TossConfirmRequest;
import com.backend.july.payment.infrastructure.toss.dto.TossPaymentResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Component
public class TossPaymentsClient {

    private final TossPaymentsProperties properties;

    public TossPaymentResponse confirm(
            String paymentKey,
            String orderId,
            java.math.BigDecimal amount
    ) {
        TossConfirmRequest request = new TossConfirmRequest(
                paymentKey,
                orderId,
                amount
        );

        return restClient()
                .post()
                .uri("/v1/payments/confirm")
                .body(request)
                .retrieve()
                .body(TossPaymentResponse.class);
    }

    public TossPaymentResponse cancel(
            String paymentKey,
            String cancelReason
    ) {
        TossCancelRequest request =
                new TossCancelRequest(cancelReason);

        return restClient()
                .post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .body(request)
                .retrieve()
                .body(TossPaymentResponse.class);
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl(properties.apiUrl())
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        createAuthorizationHeader()
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    private String createAuthorizationHeader() {
        String credentials =
                properties.secretKey() + ":";

        String encoded =
                Base64.getEncoder()
                        .encodeToString(
                                credentials.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        return "Basic " + encoded;
    }
}
