package ru.yandex.practicum.commerce.cart.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.practicum.commerce.cart.exception.CartException;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.cart.repository.ShoppingCartRepository;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {
    private final ShoppingCartRepository repository;
    private final WarehouseClient warehouseClient;
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public ShoppingCartDto getCart(String username) {
        return toDto(getOrCreate(username));
    }

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        ShoppingCartEntity cart = getOrCreate(username);
        products.forEach((productId, quantity) -> cart.getProducts().merge(productId, quantity, Long::sum));
        checkWarehouse(cart);
        return toDto(repository.save(cart));
    }

    @Transactional
    public void deactivate(String username) {
        ShoppingCartEntity cart = getOrCreate(username);
        cart.setActive(false);
    }

    @Transactional
    public ShoppingCartDto remove(String username, List<UUID> productIds) {
        ShoppingCartEntity cart = getOrCreate(username);
        boolean removed = productIds.stream().map(cart.getProducts()::remove).anyMatch(Objects::nonNull);
        if (!removed) {
            throw new CartException("No requested products in shopping cart", HttpStatus.BAD_REQUEST);
        }
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCartEntity cart = getOrCreate(username);
        if (!cart.getProducts().containsKey(request.getProductId())) {
            throw new CartException("No requested product in shopping cart", HttpStatus.BAD_REQUEST);
        }
        if (request.getNewQuantity() == 0) {
            cart.getProducts().remove(request.getProductId());
        } else {
            cart.getProducts().put(request.getProductId(), request.getNewQuantity());
            checkWarehouse(cart);
        }
        return toDto(cart);
    }

    private ShoppingCartEntity getOrCreate(String username) {
        validateUsername(username);
        return repository.findFirstByUsernameAndActiveTrue(username).orElseGet(() -> {
            ShoppingCartEntity cart = new ShoppingCartEntity();
            cart.setShoppingCartId(UUID.randomUUID());
            cart.setUsername(username);
            cart.setActive(true);
            return repository.save(cart);
        });
    }

    private void validateUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new CartException("Username must not be blank", HttpStatus.UNAUTHORIZED);
        }
    }

    private void checkWarehouse(ShoppingCartEntity cart) {
        ShoppingCartDto cartDto = toDto(cart);
        try {
            warehouseClient.checkProductQuantityEnoughForShoppingCart(cartDto);
        } catch (FeignException.BadRequest exception) {
            throw new CartException("Product quantity is not enough in warehouse", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException exception) {
            checkWarehouseThroughDiscovery(cartDto);
        }
    }

    private void checkWarehouseThroughDiscovery(ShoppingCartDto cartDto) {
        for (int attempt = 0; attempt < 5; attempt++) {
            var instances = discoveryClient.getInstances("warehouse");
            if (!instances.isEmpty()) {
                try {
                    String url = instances.getFirst().getUri() + "/api/v1/warehouse/check";
                    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST,
                            new HttpEntity<>(cartDto), Void.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return;
                    }
                } catch (HttpClientErrorException.BadRequest exception) {
                    throw new CartException("Product quantity is not enough in warehouse", HttpStatus.BAD_REQUEST);
                } catch (RuntimeException ignored) {
                    // Try another registry snapshot before reporting warehouse as unavailable.
                }
            }
            sleepBeforeRetry();
        }
        throw new CartException("Warehouse service is unavailable, try again later", HttpStatus.SERVICE_UNAVAILABLE);
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private ShoppingCartDto toDto(ShoppingCartEntity cart) {
        return new ShoppingCartDto(cart.getShoppingCartId(), Map.copyOf(cart.getProducts()));
    }
}
