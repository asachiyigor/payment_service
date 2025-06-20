package faang.school.payment_service.controller;

import faang.school.payment_service.dto.PaymentStatus;
import faang.school.payment_service.dto.currency.Currency;
import faang.school.payment_service.dto.currency.CurrencyPaymentRequest;
import faang.school.payment_service.dto.currency.CurrencyPaymentResponse;
import faang.school.payment_service.dto.payment.PaymentOperationDto;
import faang.school.payment_service.service.currency.CurrencyExchangeService;


import faang.school.payment_service.service.payment.PaymentService;
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
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public CurrencyPaymentResponse sendPayment(@RequestBody CurrencyPaymentRequest dto) {
        return paymentService.processPayment(dto);
    }

    @PostMapping("/initiate")
    public CompletableFuture<PaymentOperationDto> initiatePayment(@Valid @RequestBody PaymentOperationDto request) {
        return paymentService.initiatePaymentAsync(request);
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