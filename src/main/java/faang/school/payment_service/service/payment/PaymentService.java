package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import faang.school.payment_service.exception.InvalidPaymentAmountException;
import faang.school.payment_service.exception.PaymentNotFoundException;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import faang.school.payment_service.mapper.payment.PaymentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import faang.school.payment_service.redis.RedisMessageBroker;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentOperationRepository paymentOperationRepository;
    private final PaymentMapper paymentMapper;
    private final RedisMessageBroker redisMessageBroker;

    public Long initiatePayment(PaymentOperationDto request) {
        log.info("Initiating payment: owner={}, recipient={}, amount={}",
                request.getOwnerAccId(),
                request.getRecipientAccId(),
                request.getAmount());

        validatePaymentRequest(request);
        PaymentOperationDto paymentOperationDto = createPaymentOperationDto(request);
        PaymentOperation savedPaymentOperation = paymentOperationRepository
                .save(paymentMapper.toEntity(paymentOperationDto));
        PaymentOperationDto savedDto = paymentMapper.toDto(savedPaymentOperation);
        log.info("Payment initiated successfully: paymentId={}", savedDto.getId());
        redisMessageBroker.sendAndReceive(savedDto, 10, TimeUnit.SECONDS);
        return savedDto.getId();
    }

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
//        paymentOperationDto.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment cancelled successfully: paymentId={}", paymentId);
//        cancellationEventPublisher.publishCancellationEvent(paymentOperationDto);
    }

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
//        paymentOperationDto.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment confirmed successfully: paymentId={}", paymentId);
//        clearingEventPublisher.publishClearingEvent(paymentOperationDto);
    }

    public PaymentOperationDto processPayment(PaymentOperationDto payment) {
        try {
            return redisMessageBroker.sendAndReceive(payment, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    private void validatePaymentRequest(PaymentOperationDto request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid payment amount: amount={}", request.getAmount());
            throw new InvalidPaymentAmountException("Payment amount must be positive");
        }
        if (request.getOwnerAccId() == 0 || request.getRecipientAccId() == 0) {
            log.error("Invalid accounts: sender={}, recipient={}",
                    request.getOwnerAccId(),
                    request.getRecipientAccId());
            throw new InvalidPaymentAmountException("Sender and recipient accounts must be specified");
        }
        log.debug("Payment request validation passed");
    }

    private PaymentOperationDto createPaymentOperationDto(PaymentOperationDto request) {
        return PaymentOperationDto.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .ownerAccId(request.getOwnerAccId())
                .recipientAccId(request.getRecipientAccId())
                .status(PaymentStatus.PENDING)
                .operationType(PaymentOperationType.INITIATE)
                .clearScheduledAt(LocalDateTime.now().toString())
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .build();
    }
}