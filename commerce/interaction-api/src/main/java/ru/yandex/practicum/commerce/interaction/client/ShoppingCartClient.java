package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartClient {
    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getShoppingCart(@RequestParam String username);

    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto addProductToShoppingCart(@RequestParam String username,
                                             @RequestBody Map<UUID, Long> products);

    @DeleteMapping("/api/v1/shopping-cart")
    void deactivateCurrentShoppingCart(@RequestParam String username);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam String username,
                                           @RequestBody List<UUID> products);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam String username,
                                          @Valid @RequestBody ChangeProductQuantityRequest request);
}
