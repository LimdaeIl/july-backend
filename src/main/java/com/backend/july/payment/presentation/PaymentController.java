package com.backend.july.payment.presentation;

import com.backend.july.auth.infrastructure.security.principal.LoginMember;
import com.backend.july.common.response.ApiResponse;
import com.backend.july.payment.application.CancelPaymentService;
import com.backend.july.payment.application.ConfirmPaymentService;
import com.backend.july.payment.application.PreparePaymentService;
import com.backend.july.payment.presentation.dto.request.CancelPaymentRequest;
import com.backend.july.payment.presentation.dto.request.ConfirmPaymentRequest;
import com.backend.july.payment.presentation.dto.request.PreparePaymentRequest;
import com.backend.july.payment.presentation.dto.response.ConfirmPaymentResponse;
import com.backend.july.payment.presentation.dto.response.PreparePaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
public class PaymentController {

    private final PreparePaymentService preparePaymentService;
    private final ConfirmPaymentService confirmPaymentService;
    private final CancelPaymentService cancelPaymentService;

    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PreparePaymentResponse>> prepare(
            @AuthenticationPrincipal LoginMember member,
            @RequestBody @Valid PreparePaymentRequest request
    ) {
        PreparePaymentResponse response =
                preparePaymentService.prepare(
                        member.memberId(),
                        request.orderId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        "결제 준비에 성공했습니다.",
                        response
                ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<ConfirmPaymentResponse>> confirm(
            @AuthenticationPrincipal LoginMember member,
            @RequestBody @Valid ConfirmPaymentRequest request
    ) {
        ConfirmPaymentResponse response =
                confirmPaymentService.confirm(
                        member.memberId(),
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.ok(
                        "결제 승인에 성공했습니다.",
                        response
                )
        );
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal LoginMember member,
            @PathVariable Long paymentId,
            @RequestBody @Valid CancelPaymentRequest request
    ) {
        cancelPaymentService.cancel(
                member.memberId(),
                paymentId,
                request.cancelReason()
        );

        return ResponseEntity.noContent().build();
    }
}
