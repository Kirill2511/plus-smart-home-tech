package ru.yandex.practicum.commerce.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.cart.service.ShoppingCartService;
import ru.yandex.practicum.commerce.interaction.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interaction.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {
    private final ShoppingCartService service;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        return service.getCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, @RequestBody Map<UUID, Long> products) {
        return service.addProducts(username, products);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        service.deactivate(username);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, @RequestBody List<UUID> products) {
        return service.remove(username, products);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username,
                                                 @Valid @RequestBody ChangeProductQuantityRequest request) {
        return service.changeQuantity(username, request);
    }
}
