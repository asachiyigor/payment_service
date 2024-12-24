package faang.school.payment_service.service.payment.strategy;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitiatePaymentStrategy implements PaymentUpdateStrategy {
    private final PaymentOperationRepository paymentOperationRepository;
    private final CreateBasePaymentOperationDto createBaseDto;

    @Override
    public void updatePayment(PaymentOperation payment, PaymentOperationDto paymentData) {
        log.info("Executing INITIATE payment update strategy for payment: {}", payment);
        createBaseDto.baseUpdatePayment(payment, paymentData);
        payment.setStatus(PaymentStatus.AUTHORIZED);
        paymentOperationRepository.save(payment);
        log.info("Successfully updated INITIATE payment with ID: {}", payment.getId());
    }
}