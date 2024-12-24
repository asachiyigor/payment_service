package faang.school.payment_service.scheduler;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import faang.school.payment_service.mapper.payment.PaymentMapper;
import faang.school.payment_service.publisher.PaymentMessageEventPublisher;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentScheduler {
    private final PaymentOperationRepository paymentOperationRepository;
    private final PaymentMessageEventPublisher paymentMessageEventPublisher;
    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    public void processScheduledPayments() {
        LocalDateTime now = LocalDateTime.now();
        List<PaymentOperation> scheduledPayments = findPaymentsForConfirmation(now);

        for (PaymentOperation payment : scheduledPayments) {
            try {
                confirmScheduledPayment(payment);
            } catch (Exception e) {
                log.error("Failed to process scheduled payment confirmation {}: {}",
                        payment.getId(), e.getMessage(), e);
            }
        }
    }

    private List<PaymentOperation> findPaymentsForConfirmation(LocalDateTime now) {
        return paymentOperationRepository.findScheduledPaymentsForConfirmation(
                PaymentOperationType.TIMECONFIRM,
                now
        );
    }

    private void confirmScheduledPayment(PaymentOperation payment) {
        log.info("Confirming scheduled payment: paymentId={}", payment.getId());

        PaymentOperationDto paymentOperationConfirmDto = paymentMapper.toDto(payment);
        paymentOperationConfirmDto.setStatus(PaymentStatus.SUCCESS);
        paymentOperationConfirmDto.setOperationType(PaymentOperationType.CONFIRM);

        paymentOperationRepository.save(paymentMapper.toEntity(paymentOperationConfirmDto));

        paymentMessageEventPublisher.sendRequest(paymentOperationConfirmDto, 3, TimeUnit.SECONDS);

        log.info("Scheduled payment confirmation initiated with: Id: {}, Status: {} and Type: {}",
                paymentOperationConfirmDto.getId(),
                paymentOperationConfirmDto.getStatus(),
                paymentOperationConfirmDto.getOperationType());
    }
}