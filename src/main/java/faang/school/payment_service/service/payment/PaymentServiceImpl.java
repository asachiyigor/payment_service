package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentInitiateRequest;
import faang.school.payment_service.dto.PaymentOperationDto;
import faang.school.payment_service.dto.PaymentOperationType;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.exception.InvalidPaymentAmountException;
import faang.school.payment_service.exception.PaymentNotFoundException;
import faang.school.payment_service.publisher.PaymentEventPublisher;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentOperationRepository paymentOperationRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Override
    public Long initiatePayment(PaymentInitiateRequest request) {
        // Validate input
        validatePaymentRequest(request);

        // Create pending payment operation
        PaymentOperationDto paymentOperation = PaymentOperationDto.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .senderAccountId(request.getSenderAccountId())
                .recipientAccountId(request.getRecipientAccountId())
                .status(PaymentStatus.PENDING)
                .operationType(PaymentOperationType.INITIATE)
                .clearScheduledAt(request.getClearScheduledAt())
                .createdAt(LocalDateTime.now())
                .build();

        // Save to database
        paymentOperation = paymentOperationRepository.save(paymentOperation);

        // Produce authorization event to account service
        paymentEventPublisher.publishAuthorizationEvent(paymentOperation);

        return paymentOperation.getId();
    }

    @Override
    public void cancelPayment(Long paymentId) {
        PaymentOperationDto paymentOperation = paymentOperationRepository
                .findByIdAndStatus(paymentId, PaymentStatus.PENDING)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        paymentOperation.setStatus(PaymentStatus.CANCELLED);
        paymentOperation.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentOperation);

        // Produce cancellation event to account service
        paymentEventPublisher.publishCancellationEvent(paymentOperation);
    }

    @Override
    public void confirmPayment(Long paymentId) {
        PaymentOperationDto paymentOperation = paymentOperationRepository
                .findByIdAndStatus(paymentId, PaymentStatus.PENDING)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        paymentOperation.setStatus(PaymentStatus.SUCCESS);
        paymentOperation.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentOperation);

        // Produce clearing event to account service
        paymentEventPublisher.publishClearingEvent(paymentOperation);
    }

    private void validatePaymentRequest(PaymentInitiateRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAmountException("Payment amount must be positive");
        }

        // Additional validation can be added here
    }
}