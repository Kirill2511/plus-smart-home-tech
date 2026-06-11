package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;

import java.math.BigDecimal;

@FeignClient(name = "delivery")
public interface DeliveryClient {
    @PutMapping("/api/v1/delivery")
    DeliveryDto planDelivery(@Valid @RequestBody DeliveryDto delivery);

    @PostMapping("/api/v1/delivery/successful")
    void deliverySuccessful(@RequestBody String deliveryId);

    @PostMapping("/api/v1/delivery/picked")
    void deliveryPicked(@RequestBody String deliveryId);

    @PostMapping("/api/v1/delivery/failed")
    void deliveryFailed(@RequestBody String deliveryId);

    @PostMapping("/api/v1/delivery/cost")
    BigDecimal deliveryCost(@Valid @RequestBody OrderDto order);
}
