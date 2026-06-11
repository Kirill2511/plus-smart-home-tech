package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.PaymentDto;

import java.math.BigDecimal;

@FeignClient(name = "payment")
public interface PaymentClient {
    @PostMapping("/api/v1/payment")
    PaymentDto payment(@Valid @RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/totalCost")
    BigDecimal getTotalCost(@Valid @RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/refund")
    void paymentSuccess(@RequestBody String paymentId);

    @PostMapping("/api/v1/payment/productCost")
    BigDecimal productCost(@Valid @RequestBody OrderDto order);

    @PostMapping("/api/v1/payment/failed")
    void paymentFailed(@RequestBody String paymentId);
}
