package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.payment.PaymentInitiateRequest;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {
    Long initiatePayment(PaymentInitiateRequest request);
    void cancelPayment(Long paymentId);
    void confirmPayment(Long paymentId);
}