package faang.school.payment_service.service.payment.strategy;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Slf4j
@Component
public class CreateBasePaymentOperationDto {
    public void baseUpdatePayment(PaymentOperation payment, PaymentOperationDto paymentData) {
        log.info("Executing BASE payment with ID: {}", payment.getId());
        payment.setAmount(paymentData.getAmount());
        payment.setCurrency(paymentData.getCurrency());
        payment.setOwnerAccId(paymentData.getOwnerAccId());
        payment.setRecipientAccId(paymentData.getRecipientAccId());
        payment.setOperationType(paymentData.getOperationType());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setStatus(paymentData.getStatus());
        log.info("Successfully updated BASE payment with ID: {}", payment.getId());
    }
}