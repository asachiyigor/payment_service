package faang.school.payment_service.service.payment.strategy;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.payment.PaymentOperationDto;

public interface PaymentUpdateStrategy {
    void updatePayment(PaymentOperation payment, PaymentOperationDto paymentData);
}