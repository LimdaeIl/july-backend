package com.backend.july.payment.infrastructure;

import com.backend.july.payment.domain.Payment;
import com.backend.july.payment.domain.PaymentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findFirstByOrderIdAndStatusOrderByIdDesc(Long orderId, PaymentStatus status);

    boolean existsByPaymentKey(String paymentKey);
}
