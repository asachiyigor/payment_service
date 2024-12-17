package faang.school.payment_service.controller;

import faang.school.payment_service.dto.Currency;
import faang.school.payment_service.dto.PaymentInitiateRequest;
import faang.school.payment_service.dto.PaymentSendRequest;
import faang.school.payment_service.dto.PaymentSendResponse;
import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.service.currency.CurrencyExchangeService;
import faang.school.payment_service.service.payment.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final CurrencyExchangeService currencyExchangeService;
    private final PaymentServiceImpl paymentService;

    @PostMapping("/send")
    public PaymentSendResponse sendPayment(@RequestBody @Validated PaymentSendRequest dto) {
        BigDecimal currencyAmount = currencyExchangeService.convertCurrency(dto);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String formattedSum = decimalFormat.format(currencyAmount);
        int verificationCode = new Random().nextInt(1000, 10000);
        String message = String.format("Dear friend! Thank you for your purchase! " +
                        "Your payment of %s USD was accepted.",
                formattedSum);

        return new PaymentSendResponse(
                PaymentStatus.SUCCESS,
                verificationCode,
                dto.paymentNumber(),
                dto.amount(),
                dto.currencyCode(),
                formattedSum,
                Currency.USD,
                message
        );
    }

    @PostMapping("/initiate")
    public ResponseEntity<Long> initiatePayment(@Valid @RequestBody PaymentInitiateRequest request) {
        Long paymentId = paymentService.initiatePayment(request);
        return ResponseEntity.accepted().body(paymentId);
    }

    @PostMapping("/confirm/{paymentId}")
    public ResponseEntity<Void> confirmPayment(@PathVariable Long paymentId) {
        paymentService.confirmPayment(paymentId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long paymentId) {
        paymentService.cancelPayment(paymentId);
        return ResponseEntity.accepted().build();
    }
}