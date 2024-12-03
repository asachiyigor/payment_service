package faang.school.paymentservice.controller;

import faang.school.paymentservice.dto.Currency;
import faang.school.paymentservice.dto.PaymentRequest;
import faang.school.paymentservice.dto.PaymentResponse;
import faang.school.paymentservice.dto.PaymentStatus;
import faang.school.paymentservice.service.currency.CurrencyExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Random;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {
    private final CurrencyExchangeService currencyExchangeService;

    @PostMapping("/payment")
    public PaymentResponse sendPayment(@RequestBody @Validated PaymentRequest dto) {
        BigDecimal currencyAmount = currencyExchangeService.convertCurrency(dto);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String formattedSum = decimalFormat.format(currencyAmount);
        int verificationCode = new Random().nextInt(1000, 10000);
        String message = String.format("Dear friend! Thank you for your purchase! " +
                        "Your payment of %s USD was accepted.",
                formattedSum);

        return new PaymentResponse(
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
}