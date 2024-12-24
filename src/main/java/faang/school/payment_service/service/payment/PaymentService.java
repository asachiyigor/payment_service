package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import faang.school.payment_service.exception.InvalidPaymentAmountException;
import faang.school.payment_service.exception.PaymentNotFoundException;
import faang.school.payment_service.mapper.payment.PaymentMapper;
import faang.school.payment_service.publisher.PaymentMessageEventPublisher;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Async
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentOperationRepository paymentOperationRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentMessageEventPublisher paymentMessageEventPublisher;

    //    @Retryable(retryFor = OptimisticLockingFailureException.class, backoff = @Backoff(delay = 3000L))
//    public PaymentOperationDto processPayment(PaymentOperationDto request) throws IllegalArgumentException {
//        try {
//            switch (request.getOperationType()) {
//                case INITIATE: {
//
//                    log.info("Initiating payment: owner={}, recipient={}, amount={}",
//                            request.getOwnerAccId(),
//                            request.getRecipientAccId(),
//                            request.getAmount());
//
//                    validatePaymentRequest(request);
//                    PaymentOperationDto paymentOperationDto = createPaymentOperationDto(request);
//                    PaymentOperation savedPaymentOperation = paymentOperationRepository
//                            .save(paymentMapper.toEntity(paymentOperationDto));
//                    PaymentOperationDto savedDto = paymentMapper.toDto(savedPaymentOperation);
//                    log.info("Payment initiated successfully: paymentId={}", savedDto.getId());
//                    paymentMessageEventPublisher.sendRequest(savedDto, 10, TimeUnit.SECONDS);
//
//                }
//                case CANCEL: {
//
//                    // TODO ОТМЕНИТЬ ПЛАТЕЖ, ВЕРНУТЬ АВТОРИЗОВАННЫЕ ДЕНЬГИ
//
//                }
//                case CONFIRM: {
//
//                    // TODO ПОДТВЕРДИТь ПЛАТЕЖ
//
//                }
//                default: {
//                    throw new IllegalArgumentException("Unsupported operation type: " + request.getOperationType());
//                }
//            }
//        } catch (Exception e) {
//            request.setStatus(PaymentStatus.FAILED);
//            paymentOperationRepository.save(paymentMapper.toEntity(request));
//            throw e;
//        }
//    }

    public CompletableFuture<PaymentOperationDto> initiatePaymentAsync(PaymentOperationDto request) {
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
        paymentMessageEventPublisher.sendRequest(savedDto, 3, TimeUnit.SECONDS);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
                return paymentOperationRepository
                        .findByIdAndStatus(savedDto.getId(), PaymentStatus.AUTHORIZED)
                        .map(paymentMapper::toDto)
                        .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
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
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment cancelled successfully: paymentId={}", paymentId);
    }

    public void confirmPayment(Long paymentId) {
        log.info("Attempting to confirm payment: paymentId={}", paymentId);
        PaymentOperation paymentOperation = paymentOperationRepository
                .findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED)
                .orElseThrow(() -> {
                    log.error("Payment not found for confirmation: paymentId={}", paymentId);
                    return new PaymentNotFoundException("Payment not found");
                });
        PaymentOperationDto paymentOperationDto = paymentMapper.toDto(paymentOperation);
        paymentOperationDto.setStatus(PaymentStatus.SUCCESS);
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment confirmed successfully: paymentId={}", paymentId);
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

    public void updatePaymentOperation(PaymentOperationDto paymentData) {
        try {
            Optional<PaymentOperation> paymentOptional =
                    paymentOperationRepository.findByIdAndStatus(paymentData.getId(), PaymentStatus.PENDING);

            if (paymentOptional.isPresent()) {
                PaymentOperation payment = paymentOptional.get();

                payment.setAmount(paymentData.getAmount());
                payment.setCurrency(paymentData.getCurrency());
                payment.setOwnerAccId(paymentData.getOwnerAccId());
                payment.setRecipientAccId(paymentData.getRecipientAccId());
                payment.setOperationType(paymentData.getOperationType());
                payment.setClearScheduledAt(LocalDateTime.parse(paymentData.getClearScheduledAt()));
                payment.setStatus(paymentData.getStatus());
                payment.setUpdatedAt(LocalDateTime.now());

                paymentOperationRepository.save(payment);

                log.info("Successfully updated payment_operation with ID: {}",
                        paymentData.getId());
            } else {
                log.warn("Payment operation with ID: {} and status PENDING not found",
                        paymentData.getId());
            }
        } catch (Exception e) {
            log.error("Error updating payment_operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update payment_operation", e);
        }
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