package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import faang.school.payment_service.service.payment.strategy.CancelledPaymentStrategy;
import faang.school.payment_service.service.payment.strategy.ConfirmPaymentStrategy;
import faang.school.payment_service.service.payment.strategy.CreateBasePaymentOperationDto;
import faang.school.payment_service.service.payment.strategy.InitiatePaymentStrategy;
import faang.school.payment_service.service.payment.strategy.PaymentUpdateStrategy;
import faang.school.payment_service.service.payment.strategy.TimeConfirmPaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class PaymentOperationService {
    private final PaymentOperationRepository paymentOperationRepository;
    private final Map<PaymentStatus, PaymentUpdateStrategy> strategies;
    private final CreateBasePaymentOperationDto createBaseDto;

    public PaymentOperationService(PaymentOperationRepository paymentOperationRepository, CreateBasePaymentOperationDto createBaseDto) {
        this.paymentOperationRepository = paymentOperationRepository;
        this.createBaseDto = createBaseDto;
        this.strategies = new HashMap<>();
        initializeStrategies();
    }

    private void initializeStrategies() {
        strategies.put(PaymentStatus.PENDING, new InitiatePaymentStrategy(paymentOperationRepository, createBaseDto));
        strategies.put(PaymentStatus.AUTHORIZED, new TimeConfirmPaymentStrategy(paymentOperationRepository, createBaseDto));
        strategies.put(PaymentStatus.SUCCESS, new ConfirmPaymentStrategy(paymentOperationRepository, createBaseDto));
        strategies.put(PaymentStatus.CANCELLED, new CancelledPaymentStrategy(paymentOperationRepository, createBaseDto));

    }

    public void updatePaymentOperation(PaymentOperationDto paymentData) {
        try {
            PaymentStatus targetStatus = PaymentStatus.valueOf(String.valueOf(paymentData.getStatus()));
            PaymentStatus requiredCurrentStatus = getRequiredCurrentStatus(paymentData.getOperationType());

            PaymentUpdateStrategy strategy = Optional.ofNullable(strategies.get(targetStatus))
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported payment status: " + targetStatus));

            PaymentOperation payment = paymentOperationRepository
                    .findByIdAndStatus(paymentData.getId(), requiredCurrentStatus)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Payment operation with ID: %s and status %s not found",
                                    paymentData.getId(), requiredCurrentStatus)));

            strategy.updatePayment(payment, paymentData);

        } catch (IllegalArgumentException e) {
            log.error("Invalid payment data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating payment operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update payment operation", e);
        }
    }

    private PaymentStatus getRequiredCurrentStatus(PaymentOperationType operationType) {
        return switch (operationType) {
            case INITIATE -> PaymentStatus.PENDING;
            case TIMECONFIRM, CONFIRM, CANCEL -> PaymentStatus.AUTHORIZED;
        };
    }
}