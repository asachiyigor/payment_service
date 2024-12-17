package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentInitiateRequest;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import faang.school.payment_service.exception.InvalidPaymentAmountException;
import faang.school.payment_service.exception.PaymentNotFoundException;
import faang.school.payment_service.publisher.PaymentsDMSRecipientPublisher;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import faang.school.payment_service.mapper.payment.PaymentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentOperationRepository paymentOperationRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentsDMSRecipientPublisher paymentsDMSRecipientPublisher;

    @Override
    public Long initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment: sender={}, recipient={}, amount={}",
                request.getOwnerId(),
                request.getRecipientId(),
                request.getAmount());

        validatePaymentRequest(request);
        PaymentOperationDto paymentOperationDto = createPaymentOperationDto(request);
        PaymentOperation savedPaymentOperation = paymentOperationRepository
                .save(paymentMapper.toEntity(paymentOperationDto));
        PaymentOperationDto savedDto = paymentMapper.toDto(savedPaymentOperation);
        log.info("Payment initiated successfully: paymentId={}", savedDto.getId());
        paymentsDMSRecipientPublisher.publishPaymentsDMSRecipientEvent(savedDto);
        log.info("короче отправили");
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
//        paymentOperationDto.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment cancelled successfully: paymentId={}", paymentId);
//        cancellationEventPublisher.publishCancellationEvent(paymentOperationDto);
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
//        paymentOperationDto.setUpdatedAt(LocalDateTime.now());
        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationDto));
        log.info("Payment confirmed successfully: paymentId={}", paymentId);
//        clearingEventPublisher.publishClearingEvent(paymentOperationDto);
    }

    private void validatePaymentRequest(PaymentInitiateRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid payment amount: amount={}", request.getAmount());
            throw new InvalidPaymentAmountException("Payment amount must be positive");
        }
        if (request.getOwnerId() == 0 || request.getRecipientId() == 0) {
            log.error("Invalid accounts: sender={}, recipient={}",
                    request.getOwnerId(),
                    request.getRecipientId());
            throw new InvalidPaymentAmountException("Sender and recipient accounts must be specified");
        }
        log.debug("Payment request validation passed");
    }

    private PaymentOperationDto createPaymentOperationDto(PaymentInitiateRequest request) {
        return PaymentOperationDto.builder()
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .ownerId(request.getOwnerId())
                .recipientId(request.getRecipientId())
                .status(PaymentStatus.PENDING)
                .operationType(PaymentOperationType.INITIATE)
//                .clearScheduledAt(LocalDateTime.now().plusMinutes(2))
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
                .build();
    }
}