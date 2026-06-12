package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {
    private final WarehouseService service;

    @Override
    public void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request) {
        service.newProduct(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto shoppingCart) {
        return service.check(shoppingCart);
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest request) {
        return service.assemblyProductsForOrder(request);
    }

    @Override
    public void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request) {
        service.addProduct(request);
    }

    @Override
    public void acceptReturn(@RequestBody Map<UUID, Long> products) {
        service.acceptReturn(products);
    }

    @Override
    public void shippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest request) {
        service.shippedToDelivery(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return service.getAddress();
    }
}
