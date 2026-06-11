package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.ProductReturnRequest;

import java.util.List;

@FeignClient(name = "order")
public interface OrderClient {
    @GetMapping("/api/v1/order")
    List<OrderDto> getClientOrders(@RequestParam String username);

    @PutMapping("/api/v1/order")
    OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request);

    @PostMapping("/api/v1/order/return")
    OrderDto productReturn(@Valid @RequestBody ProductReturnRequest request);

    @PostMapping("/api/v1/order/payment")
    OrderDto payment(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment/start")
    OrderDto startPayment(@RequestBody String orderId);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto paymentFailed(@RequestBody String orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto delivery(@RequestBody String orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto deliveryFailed(@RequestBody String orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto complete(@RequestBody String orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalCost(@RequestBody String orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody String orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto assembly(@RequestBody String orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    OrderDto assemblyFailed(@RequestBody String orderId);
}
