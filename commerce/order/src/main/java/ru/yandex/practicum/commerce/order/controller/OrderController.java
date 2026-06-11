package ru.yandex.practicum.commerce.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderClient {
    private final OrderService service;

    @Override
    public List<OrderDto> getClientOrders(String username) {
        return service.getClientOrders(username);
    }

    @Override
    public OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request) {
        return service.create(request);
    }

    @Override
    public OrderDto productReturn(@Valid @RequestBody ProductReturnRequest request) {
        return service.returnProducts(request);
    }

    @Override
    public OrderDto payment(@RequestBody String orderId) {
        return service.markPaymentSuccess(orderId);
    }

    @Override
    public OrderDto startPayment(@RequestBody String orderId) {
        return service.startPayment(orderId);
    }

    @Override
    public OrderDto paymentFailed(@RequestBody String orderId) {
        return service.markPaymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(@RequestBody String orderId) {
        return service.markDeliverySuccess(orderId);
    }

    @Override
    public OrderDto deliveryFailed(@RequestBody String orderId) {
        return service.markDeliveryFailed(orderId);
    }

    @Override
    public OrderDto complete(@RequestBody String orderId) {
        return service.complete(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(@RequestBody String orderId) {
        return service.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(@RequestBody String orderId) {
        return service.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(@RequestBody String orderId) {
        return service.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(@RequestBody String orderId) {
        return service.assemblyFailed(orderId);
    }
}
