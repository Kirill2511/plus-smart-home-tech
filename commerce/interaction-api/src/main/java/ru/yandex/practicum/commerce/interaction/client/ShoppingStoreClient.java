package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.interaction.enums.ProductCategory;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient {
    @GetMapping("/api/v1/shopping-store")
    Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                 @RequestParam(required = false, defaultValue = "0") Integer page,
                                 @RequestParam(required = false, defaultValue = "20") Integer size,
                                 @RequestParam(required = false) List<String> sort);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@Valid @RequestBody ProductDto product);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@Valid @RequestBody ProductDto product);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean setProductQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);
}
