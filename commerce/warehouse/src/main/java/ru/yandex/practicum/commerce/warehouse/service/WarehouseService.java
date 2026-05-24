package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.exception.WarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final WarehouseProductRepository repository;

    @Transactional
    public void newProduct(NewProductInWarehouseRequest request) {
        if (repository.existsById(request.getProductId())) {
            throw new WarehouseException("Product already registered in warehouse", HttpStatus.BAD_REQUEST);
        }
        WarehouseProductEntity entity = new WarehouseProductEntity();
        entity.setProductId(request.getProductId());
        entity.setFragile(Boolean.TRUE.equals(request.getFragile()));
        entity.setWidth(request.getDimension().getWidth());
        entity.setHeight(request.getDimension().getHeight());
        entity.setDepth(request.getDimension().getDepth());
        entity.setWeight(request.getWeight());
        entity.setQuantity(0);
        repository.save(entity);
    }

    @Transactional
    public void addProduct(AddProductToWarehouseRequest request) {
        WarehouseProductEntity entity = repository.findById(request.getProductId())
                .orElseThrow(() -> missing(request.getProductId()));
        entity.setQuantity(entity.getQuantity() + request.getQuantity());
    }

    @Transactional(readOnly = true)
    public BookedProductsDto check(ShoppingCartDto cart) {
        List<UUID> lowQuantityProducts = new ArrayList<>();
        double totalWeight = 0;
        double totalVolume = 0;
        boolean fragile = false;

        for (var entry : cart.getProducts().entrySet()) {
            WarehouseProductEntity product = repository.findById(entry.getKey())
                    .orElseThrow(() -> missing(entry.getKey()));
            long requested = entry.getValue();
            if (product.getQuantity() < requested) {
                lowQuantityProducts.add(entry.getKey());
            }
            totalWeight += product.getWeight() * requested;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requested;
            fragile = fragile || product.isFragile();
        }

        if (!lowQuantityProducts.isEmpty()) {
            throw new WarehouseException("Not enough products in warehouse: " + lowQuantityProducts,
                    HttpStatus.BAD_REQUEST);
        }

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }

    public AddressDto getAddress() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    private WarehouseException missing(UUID productId) {
        return new WarehouseException("Product " + productId + " is not registered in warehouse",
                HttpStatus.BAD_REQUEST);
    }
}
