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
import faang.school.payment_service.service.payment.strategy.PaymentUpdateStrategy;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
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
    private final Map<String, PaymentUpdateStrategy> strategies;

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
        paymentMessageEventPublisher.sendRequest(savedDto, 2, TimeUnit.SECONDS);

        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
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
                .findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED)
                .orElseThrow(() -> {
                    log.error("Payment not found for cancellation: paymentId={}", paymentId);
                    return new PaymentNotFoundException("Payment not found");
                });
        PaymentOperationDto paymentOperationCancelDto = paymentMapper.toDto(paymentOperation);
        paymentOperationCancelDto.setOperationType(PaymentOperationType.CANCEL);
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationCancelDto));

        paymentMessageEventPublisher.sendRequest(paymentOperationCancelDto, 3, TimeUnit.SECONDS);
        log.info("Payment cancellation initiated with: Id: {}, Status: {} and Type: {}",
                paymentOperationCancelDto.getId(),
                paymentOperationCancelDto.getStatus(),
                paymentOperationCancelDto.getOperationType());
    }

    public void confirmPayment(Long paymentId) {
        log.info("Attempting to confirm payment: paymentId={}", paymentId);

        PaymentOperation paymentOperation = paymentOperationRepository
                .findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED)
                .orElseThrow(() -> {
                    log.error("Payment not found for confirmation: paymentId={}", paymentId);
                    return new PaymentNotFoundException("Payment not found");
                });
        PaymentOperationDto paymentOperationConfirmDto = paymentMapper.toDto(paymentOperation);
        paymentOperationConfirmDto.setOperationType(PaymentOperationType.TIMECONFIRM);
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationConfirmDto));

        paymentMessageEventPublisher.sendRequest(paymentOperationConfirmDto, 3, TimeUnit.SECONDS);
        log.info("Payment confirmation initiated with: Id: {}, Status: {} and Type: {}",
                paymentOperationConfirmDto.getId(),
                paymentOperationConfirmDto.getStatus(),
                paymentOperationConfirmDto.getOperationType());
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
                .createdAt(LocalDateTime.now().toString())
                .updatedAt(LocalDateTime.now().toString())
                .build();
    }
}