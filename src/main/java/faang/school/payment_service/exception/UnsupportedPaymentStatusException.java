package faang.school.payment_service.exception;

public class UnsupportedPaymentStatusException extends RuntimeException {
    public UnsupportedPaymentStatusException(String message) {
        super(message);
    }
}
