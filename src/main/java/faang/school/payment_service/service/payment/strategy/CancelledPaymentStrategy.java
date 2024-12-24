package faang.school.payment_service.service.payment.strategy;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CancelledPaymentStrategy implements PaymentUpdateStrategy {
    private final PaymentOperationRepository paymentOperationRepository;
    private final CreateBasePaymentOperationDto createBaseDto;

    @Override
    public void updatePayment(PaymentOperation payment, PaymentOperationDto paymentData) {
        log.info("Executing CANCELLED payment with ID: {}", payment.getId());
        createBaseDto.baseUpdatePayment(payment, paymentData);
        paymentOperationRepository.save(payment);
        log.info("Successfully updated CANCELLED payment with ID: {}", payment.getId());
    }
}