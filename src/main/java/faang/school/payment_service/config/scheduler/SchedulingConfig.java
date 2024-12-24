package faang.school.payment_service.config.scheduler;

import faang.school.payment_service.scheduler.PaymentScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {
    private final PaymentScheduler paymentScheduler;

    @Scheduled(fixedDelay = 60000) // Выполняется каждую минуту
    public void schedulePaymentProcessing() {
        paymentScheduler.processScheduledPayments();
    }
}