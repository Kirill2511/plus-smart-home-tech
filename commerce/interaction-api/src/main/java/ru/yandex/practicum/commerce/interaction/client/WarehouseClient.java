package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShoppingCartDto;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse")
public interface WarehouseClient {
    @PutMapping("/api/v1/warehouse")
    void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblyProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto shoppingCart);

    @PostMapping("/api/v1/warehouse/add")
    void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/return")
    void acceptReturn(@RequestBody Map<UUID, Long> products);

    @PostMapping("/api/v1/warehouse/shipped")
    void shippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest request);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();
}
