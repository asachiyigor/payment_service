package faang.school.payment_service.client.exchange;

import faang.school.payment_service.dto.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "open-exchanges-rates", url = "${currency.exchange.url}")
public interface OpenExchangeRatesClient {

    @GetMapping
    ExchangeRateResponse getLatestRates(@RequestParam("app_id") String  appId);
}