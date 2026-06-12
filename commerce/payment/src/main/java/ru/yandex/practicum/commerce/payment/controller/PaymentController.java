package ru.yandex.practicum.commerce.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.PaymentClient;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentClient {
    private final PaymentService service;

    @Override
    public PaymentDto payment(@Valid @RequestBody OrderDto order) {
        return service.createPayment(order);
    }

    @Override
    public BigDecimal getTotalCost(@Valid @RequestBody OrderDto order) {
        return service.totalCost(order);
    }

    @Override
    public void paymentSuccess(@RequestBody String paymentId) {
        service.success(paymentId);
    }

    @Override
    public BigDecimal productCost(@Valid @RequestBody OrderDto order) {
        return service.productCost(order);
    }

    @Override
    public void paymentFailed(@RequestBody String paymentId) {
        service.failed(paymentId);
    }
}
