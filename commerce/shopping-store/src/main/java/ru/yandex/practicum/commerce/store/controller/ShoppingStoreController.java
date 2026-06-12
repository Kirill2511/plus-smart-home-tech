package ru.yandex.practicum.commerce.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.interaction.enums.ProductCategory;
import ru.yandex.practicum.commerce.store.service.ProductService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreClient {
    private final ProductService service;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Integer page, Integer size, List<String> sort) {
        return service.getProducts(category, page, size, sort);
    }

    @Override
    public ProductDto createNewProduct(@Valid @RequestBody ProductDto product) {
        return service.create(product);
    }

    @Override
    public ProductDto updateProduct(@Valid @RequestBody ProductDto product) {
        return service.update(product);
    }

    @Override
    public Boolean removeProductFromStore(@RequestBody UUID productId) {
        return service.deactivate(productId);
    }

    @Override
    public Boolean setProductQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request) {
        return service.setQuantityState(request);
    }

    @PostMapping(value = "/api/v1/shopping-store/quantityState", params = {"productId", "quantityState"})
    public Boolean setProductQuantityState(@RequestParam UUID productId,
                                           @RequestParam String quantityState) {
        return service.setQuantityState(new SetProductQuantityStateRequest(productId, quantityState));
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return service.getProduct(productId);
    }
}
