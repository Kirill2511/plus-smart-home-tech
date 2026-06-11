package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.exception.WarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.OrderBookingEntity;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private static final String[] ADDRESSES = new String[] {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final WarehouseProductRepository repository;
    private final OrderBookingRepository orderBookingRepository;

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
        entity.setQuantity(1000);
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

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        if (orderBookingRepository.existsById(request.getOrderId())) {
            throw new WarehouseException("Order " + request.getOrderId() + " is already assembled",
                    HttpStatus.BAD_REQUEST);
        }
        ShoppingCartDto cart = new ShoppingCartDto(request.getOrderId(), request.getProducts());
        BookedProductsDto bookedProducts = check(cart);
        request.getProducts().forEach((productId, quantity) -> {
            WarehouseProductEntity product = repository.findById(productId).orElseThrow(() -> missing(productId));
            product.setQuantity(product.getQuantity() - quantity);
        });

        OrderBookingEntity booking = new OrderBookingEntity();
        booking.setOrderId(request.getOrderId());
        booking.setProducts(Map.copyOf(request.getProducts()));
        booking.setDeliveryWeight(defaultDouble(bookedProducts.getDeliveryWeight()));
        booking.setDeliveryVolume(defaultDouble(bookedProducts.getDeliveryVolume()));
        booking.setFragile(Boolean.TRUE.equals(bookedProducts.getFragile()));
        orderBookingRepository.save(booking);

        return bookedProducts;
    }

    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        products.forEach((productId, quantity) -> {
            WarehouseProductEntity product = repository.findById(productId).orElseThrow(() -> missing(productId));
            product.setQuantity(product.getQuantity() + quantity);
        });
    }

    public AddressDto getAddress() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBookingEntity booking = orderBookingRepository.findById(request.getOrderId())
                .orElseThrow(() -> new WarehouseException("Order booking " + request.getOrderId() + " is not found",
                        HttpStatus.BAD_REQUEST));
        booking.setDeliveryId(request.getDeliveryId());
    }

    private WarehouseException missing(UUID productId) {
        return new WarehouseException("Product " + productId + " is not registered in warehouse",
                HttpStatus.BAD_REQUEST);
    }

    private double defaultDouble(Double value) {
        return value == null ? 0 : value;
    }
}
