package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.exception.InvalidPaymentAmountException;
import faang.school.payment_service.exception.PaymentNotFoundException;
import faang.school.payment_service.mapper.payment.PaymentMapper;
import faang.school.payment_service.publisher.PaymentMessageEventPublisher;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOperationRepository paymentOperationRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentMessageEventPublisher paymentMessageEventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentOperationDto validPaymentRequest;
    private PaymentOperation paymentOperation;

    @BeforeEach
    void setUp() {
        validPaymentRequest = PaymentOperationDto.builder()
                .amount(new BigDecimal("100.00"))
                .ownerAccId(1L)
                .recipientAccId(2L)
                .build();

        paymentOperation = new PaymentOperation();
        paymentOperation.setId(1L);
        paymentOperation.setStatus(PaymentStatus.AUTHORIZED);
    }

    @Test
    @DisplayName("Should throw exception when payment amount is negative")
    void initiatePaymentAsync_NegativeAmount_ThrowsException() {
        validPaymentRequest.setAmount(new BigDecimal("-100.00"));
        assertThrows(InvalidPaymentAmountException.class,
                () -> paymentService.initiatePaymentAsync(validPaymentRequest));
    }

    @Test
    @DisplayName("Should throw exception when account IDs are zero")
    void initiatePaymentAsync_ZeroAccountIds_ThrowsException() {
        validPaymentRequest.setOwnerAccId(0L);
        validPaymentRequest.setRecipientAccId(0L);
        assertThrows(InvalidPaymentAmountException.class,
                () -> paymentService.initiatePaymentAsync(validPaymentRequest));
    }

    @Test
    @DisplayName("Should successfully cancel existing payment")
    void cancelPayment_ExistingPayment_Success() {
        when(paymentOperationRepository.findByIdAndStatus(anyLong(), eq(PaymentStatus.AUTHORIZED)))
                .thenReturn(Optional.of(paymentOperation));
        when(paymentMapper.toDto(any(PaymentOperation.class))).thenReturn(validPaymentRequest);
        when(paymentMapper.toEntity(any(PaymentOperationDto.class))).thenReturn(paymentOperation);
        paymentService.cancelPayment(1L);
        verify(paymentOperationRepository).save(any(PaymentOperation.class));
        verify(paymentMessageEventPublisher).sendRequest(any(PaymentOperationDto.class), eq(3L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-existent payment")
    void cancelPayment_NonExistentPayment_ThrowsException() {
        when(paymentOperationRepository.findByIdAndStatus(anyLong(), eq(PaymentStatus.AUTHORIZED)))
                .thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class, () -> paymentService.cancelPayment(1L));
    }

    @Test
    @DisplayName("Should successfully confirm existing payment")
    void confirmPayment_ExistingPayment_Success() {
        when(paymentOperationRepository.findByIdAndStatus(anyLong(), eq(PaymentStatus.AUTHORIZED)))
                .thenReturn(Optional.of(paymentOperation));
        when(paymentMapper.toDto(any(PaymentOperation.class))).thenReturn(validPaymentRequest);
        when(paymentMapper.toEntity(any(PaymentOperationDto.class))).thenReturn(paymentOperation);
        paymentService.confirmPayment(1L);
        verify(paymentOperationRepository).save(any(PaymentOperation.class));
        verify(paymentMessageEventPublisher).sendRequest(any(PaymentOperationDto.class), eq(3L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should throw exception when confirming non-existent payment")
    void confirmPayment_NonExistentPayment_ThrowsException() {
        when(paymentOperationRepository.findByIdAndStatus(anyLong(), eq(PaymentStatus.AUTHORIZED)))
                .thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class, () -> paymentService.confirmPayment(1L));
    }

    @Test
    @DisplayName("Should throw exception when payment is not authorized during initiation")
    void initiatePaymentAsync_PaymentNotAuthorized_ThrowsException() throws Exception {
        when(paymentMapper.toEntity(any(PaymentOperationDto.class))).thenReturn(paymentOperation);
        when(paymentMapper.toDto(any(PaymentOperation.class))).thenReturn(validPaymentRequest);
        when(paymentOperationRepository.save(any(PaymentOperation.class))).thenReturn(paymentOperation);
        when(paymentOperationRepository.findByIdAndStatus(anyLong(), eq(PaymentStatus.AUTHORIZED)))
                .thenReturn(Optional.empty());
        CompletableFuture<PaymentOperationDto> future = paymentService.initiatePaymentAsync(validPaymentRequest);
        assertThrows(ExecutionException.class, () -> future.get(3, TimeUnit.SECONDS));
    }
}