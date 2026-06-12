package ru.yandex.practicum.commerce.delivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interaction.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryClient {
    private final DeliveryService service;

    @Override
    public DeliveryDto planDelivery(@Valid @RequestBody DeliveryDto delivery) {
        return service.plan(delivery);
    }

    @Override
    public void deliverySuccessful(@RequestBody String deliveryId) {
        service.successful(deliveryId);
    }

    @Override
    public void deliveryPicked(@RequestBody String deliveryId) {
        service.picked(deliveryId);
    }

    @Override
    public void deliveryFailed(@RequestBody String deliveryId) {
        service.failed(deliveryId);
    }

    @Override
    public BigDecimal deliveryCost(@Valid @RequestBody OrderDto order) {
        return service.calculateCost(order);
    }
}
