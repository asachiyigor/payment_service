package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentInitiateRequest;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import faang.school.payment_service.exception.InvalidPaymentAmountException;
import faang.school.payment_service.exception.PaymentNotFoundException;
import faang.school.payment_service.publisher.AuthorizationEventPublisher;
import faang.school.payment_service.publisher.CancellationEventPublisher;
import faang.school.payment_service.publisher.ClearingEventPublisher;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import faang.school.payment_service.mapper.payment.PaymentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentOperationRepository paymentOperationRepository;
    private final PaymentMapper paymentMapper;
    private final AuthorizationEventPublisher authorizationEventPublisher;
    private final CancellationEventPublisher cancellationEventPublisher;
    private final ClearingEventPublisher clearingEventPublisher;


    @Override
    public Long initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment: sender={}, recipient={}, amount={}",
                request.getSenderAccountId(),
                request.getRecipientAccountId(),
                request.getAmount());

        validatePaymentRequest(request);
        PaymentOperationDto paymentOperationDto = createPaymentOperationDto(request);
        PaymentOperation savedPaymentOperation = paymentOperationRepository
                .save(paymentMapper.toEntity(paymentOperationDto));
        PaymentOperationDto savedDto = paymentMapper.toDto(savedPaymentOperation);
        log.info("Payment initiated successfully: paymentId={}", savedDto.getId());
        authorizationEventPublisher.publishAuthorizationEvent(savedDto);
        return savedDto.getId();
    }

    @Override
    public void cancelPayment(Long paymentId) {
        log.info("Attempting to cancel payment: paymentId={}", paymentId);

        PaymentOperation paymentOperation = paymentOperationRepository
                .findByIdAndStatus(paymentId, PaymentStatus.PENDING)
                .orElseThrow(() -> {
                    log.error("Payment not found for cancellation: paymentId={}", paymentId);
                    return new PaymentNotFoundException("Payment not found");
                });
        PaymentOperationDto paymentOperationDto = paymentMapper.toDto(paymentOperation);
        paymentOperationDto.setStatus(PaymentStatus.CANCELLED);
        paymentOperationDto.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment cancelled successfully: paymentId={}", paymentId);
        cancellationEventPublisher.publishCancellationEvent(paymentOperationDto);
    }

    @Override
    public void confirmPayment(Long paymentId) {
        log.info("Attempting to confirm payment: paymentId={}", paymentId);
        PaymentOperation paymentOperation = paymentOperationRepository
                .findByIdAndStatus(paymentId, PaymentStatus.PENDING)
                .orElseThrow(() -> {
                    log.error("Payment not found for confirmation: paymentId={}", paymentId);
                    return new PaymentNotFoundException("Payment not found");
                });
        PaymentOperationDto paymentOperationDto = paymentMapper.toDto(paymentOperation);
        paymentOperationDto.setStatus(PaymentStatus.SUCCESS);
        paymentOperationDto.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment confirmed successfully: paymentId={}", paymentId);
        clearingEventPublisher.publishClearingEvent(paymentOperationDto);
    }

    private void validatePaymentRequest(PaymentInitiateRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid payment amount: amount={}", request.getAmount());
            throw new InvalidPaymentAmountException("Payment amount must be positive");
        }
        if (request.getSenderAccountId() == null || request.getRecipientAccountId() == null) {
            log.error("Invalid accounts: sender={}, recipient={}",
                    request.getSenderAccountId(),
                    request.getRecipientAccountId());
            throw new InvalidPaymentAmountException("Sender and recipient accounts must be specified");
        }
        log.debug("Payment request validation passed");
    }

    private PaymentOperationDto createPaymentOperationDto(PaymentInitiateRequest request) {
        return PaymentOperationDto.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .senderAccountId(request.getSenderAccountId())
                .recipientAccountId(request.getRecipientAccountId())
                .status(PaymentStatus.PENDING)
                .operationType(PaymentOperationType.INITIATE)
                .clearScheduledAt(request.getClearScheduledAt())
                .createdAt(LocalDateTime.now())
                .build();
    }
}