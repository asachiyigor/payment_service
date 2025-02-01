package faang.school.payment_service.exception;

public class PaymentOperationNotFoundException extends RuntimeException {

    public PaymentOperationNotFoundException(String message) {
        super(message);
    }
}