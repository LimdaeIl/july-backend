package com.backend.july.payment.infrastructure.toss;

import com.backend.july.payment.exception.PaymentErrorCode;
import com.backend.july.payment.exception.PaymentException;
import com.backend.july.payment.infrastructure.toss.dto.TossCancelRequest;
import com.backend.july.payment.infrastructure.toss.dto.TossConfirmRequest;
import com.backend.july.payment.infrastructure.toss.dto.TossPaymentResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class TossPaymentsClient {

    private static final String TOSS_CONFIRM_URL = "/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL = "/v1/payments/{paymentKey}/cancel";

    private final RestClient restClient;

    public TossPaymentsClient(TossPaymentsProperties properties) {
        String authorizationHeader = createAuthorizationHeader(properties.secretKey());

        this.restClient = RestClient.builder()
                .baseUrl(properties.apiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TossPaymentResponse confirm(String paymentKey, String orderId, BigDecimal amount) {
        TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);

        try {
            return restClient.post()
                    .uri(TOSS_CONFIRM_URL)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_CONFIRM_FAILED);
            }
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_CONFIRM_UNCERTAIN);

        } catch (RestClientException exception) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_CONFIRM_UNCERTAIN);
        }
    }

    public TossPaymentResponse cancel(String paymentKey, String cancelReason) {
        TossCancelRequest request = TossCancelRequest.from(cancelReason);

        try {
            return restClient.post()
                    .uri(TOSS_CANCEL_URL, paymentKey)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_NOT_CANCELLED);
            }
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_CANCEL_FAILED);

        } catch (RestClientException exception) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_CANCEL_FAILED);
        }
    }

    private String createAuthorizationHeader(String secretKey) {
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
