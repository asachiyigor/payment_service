package faang.school.payment_service.service.payment;

import faang.school.payment_service.dto.PaymentOperation;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.dto.payment.PaymentOperationType;
import faang.school.payment_service.exception.PaymentOperationNotFoundException;
import faang.school.payment_service.repository.payment.PaymentOperationRepository;
import faang.school.payment_service.service.payment.strategy.CreateBasePaymentOperationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOperationServiceTest {

    @Mock
    private PaymentOperationRepository paymentOperationRepository;

    @Mock
    private CreateBasePaymentOperationDto createBaseDto;

    private PaymentOperationService paymentOperationService;

    @BeforeEach
    void setUp() {
        paymentOperationService = new PaymentOperationService(paymentOperationRepository, createBaseDto);
    }

    @Test
    @DisplayName("Should successfully initiate payment with PENDING status")
    void updatePaymentOperation_InitiatePayment_Success() {
        Long paymentId = 1L;
        PaymentOperationDto paymentData = new PaymentOperationDto();
        paymentData.setId(paymentId);
        paymentData.setStatus(PaymentStatus.PENDING);
        paymentData.setOperationType(PaymentOperationType.INITIATE);
        PaymentOperation existingPayment = new PaymentOperation();
        existingPayment.setId(paymentId);
        existingPayment.setStatus(PaymentStatus.PENDING);
        when(paymentOperationRepository.findByIdAndStatus(paymentId, PaymentStatus.PENDING))
                .thenReturn(Optional.of(existingPayment));
        assertDoesNotThrow(() -> paymentOperationService.updatePaymentOperation(paymentData));
        verify(paymentOperationRepository).findByIdAndStatus(paymentId, PaymentStatus.PENDING);
        verify(paymentOperationRepository).save(any(PaymentOperation.class));
    }

    @Test
    @DisplayName("Should successfully time confirm payment with AUTHORIZED status")
    void updatePaymentOperation_TimeConfirmPayment_Success() {
        Long paymentId = 1L;
        PaymentOperationDto paymentData = new PaymentOperationDto();
        paymentData.setId(paymentId);
        paymentData.setStatus(PaymentStatus.AUTHORIZED);
        paymentData.setOperationType(PaymentOperationType.TIMECONFIRM);
        PaymentOperation existingPayment = new PaymentOperation();
        existingPayment.setId(paymentId);
        existingPayment.setStatus(PaymentStatus.AUTHORIZED);
        when(paymentOperationRepository.findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED))
                .thenReturn(Optional.of(existingPayment));
        assertDoesNotThrow(() -> paymentOperationService.updatePaymentOperation(paymentData));
        verify(paymentOperationRepository).findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED);
        verify(paymentOperationRepository).save(any(PaymentOperation.class));
    }

    @Test
    @DisplayName("Should throw exception when payment not found")
    void updatePaymentOperation_PaymentNotFound_ThrowsException() {
        Long paymentId = 1L;
        PaymentOperationDto paymentData = new PaymentOperationDto();
        paymentData.setId(paymentId);
        paymentData.setStatus(PaymentStatus.PENDING);
        paymentData.setOperationType(PaymentOperationType.INITIATE);
        when(paymentOperationRepository.findByIdAndStatus(paymentId, PaymentStatus.PENDING))
                .thenReturn(Optional.empty());
        PaymentOperationNotFoundException exception = assertThrows(PaymentOperationNotFoundException.class,
                () -> paymentOperationService.updatePaymentOperation(paymentData));
        assertTrue(exception.getMessage().contains("Payment operation with ID"));
    }

    @Test
    @DisplayName("Should throw exception when payment status is unsupported")
    void updatePaymentOperation_UnsupportedStatus_ThrowsException() {
        Long paymentId = 1L;
        PaymentOperationDto paymentData = new PaymentOperationDto();
        paymentData.setId(paymentId);
        paymentData.setStatus(null);
        paymentData.setOperationType(PaymentOperationType.INITIATE);
        assertThrows(RuntimeException.class,
                () -> paymentOperationService.updatePaymentOperation(paymentData));
    }

    @Test
    @DisplayName("Should successfully cancel payment with CANCELLED status")
    void updatePaymentOperation_CancelPayment_Success() {
        Long paymentId = 1L;
        PaymentOperationDto paymentData = new PaymentOperationDto();
        paymentData.setId(paymentId);
        paymentData.setStatus(PaymentStatus.CANCELLED);
        paymentData.setOperationType(PaymentOperationType.CANCEL);
        PaymentOperation existingPayment = new PaymentOperation();
        existingPayment.setId(paymentId);
        existingPayment.setStatus(PaymentStatus.AUTHORIZED);
        when(paymentOperationRepository.findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED))
                .thenReturn(Optional.of(existingPayment));
        assertDoesNotThrow(() -> paymentOperationService.updatePaymentOperation(paymentData));
        verify(paymentOperationRepository).findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED);
        verify(paymentOperationRepository).save(any(PaymentOperation.class));
    }

    @Test
    @DisplayName("Should successfully confirm payment with SUCCESS status")
    void updatePaymentOperation_ConfirmPayment_Success() {
        Long paymentId = 1L;
        PaymentOperationDto paymentData = new PaymentOperationDto();
        paymentData.setId(paymentId);
        paymentData.setStatus(PaymentStatus.SUCCESS);
        paymentData.setOperationType(PaymentOperationType.CONFIRM);
        PaymentOperation existingPayment = new PaymentOperation();
        existingPayment.setId(paymentId);
        existingPayment.setStatus(PaymentStatus.AUTHORIZED);
        when(paymentOperationRepository.findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED))
                .thenReturn(Optional.of(existingPayment));
        assertDoesNotThrow(() -> paymentOperationService.updatePaymentOperation(paymentData));
        verify(paymentOperationRepository).findByIdAndStatus(paymentId, PaymentStatus.AUTHORIZED);
        verify(paymentOperationRepository).save(any(PaymentOperation.class));
    }

    @Test
    @DisplayName("Should throw exception when status transition is invalid")
    void updatePaymentOperation_InvalidStatusTransition_ThrowsException() {
        Long paymentId = 1L;
        PaymentOperationDto paymentData = new PaymentOperationDto();
        paymentData.setId(paymentId);
        paymentData.setStatus(PaymentStatus.SUCCESS);
        paymentData.setOperationType(PaymentOperationType.INITIATE);
        assertThrows(PaymentOperationNotFoundException.class,
                () -> paymentOperationService.updatePaymentOperation(paymentData));
        verify(paymentOperationRepository, never()).save(any(PaymentOperation.class));
    }
}