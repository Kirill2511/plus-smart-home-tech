package ru.yandex.practicum.commerce.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryException;
import ru.yandex.practicum.commerce.delivery.model.AddressEmbeddable;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.enums.DeliveryState;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private static final BigDecimal BASE_COST = new BigDecimal("5.0");
    private static final BigDecimal FRAGILE_RATE = new BigDecimal("0.2");
    private static final BigDecimal WEIGHT_RATE = new BigDecimal("0.3");
    private static final BigDecimal VOLUME_RATE = new BigDecimal("0.2");
    private static final BigDecimal ADDRESS_RATE = new BigDecimal("0.2");

    private final DeliveryRepository repository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Transactional
    public DeliveryDto plan(DeliveryDto request) {
        if (request.getOrderId() == null) {
            throw new DeliveryException("Order id is required", HttpStatus.BAD_REQUEST);
        }
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setDeliveryId(UUID.randomUUID());
        delivery.setOrderId(request.getOrderId());
        delivery.setFromAddress(toEmbeddable(request.getFromAddress()));
        delivery.setToAddress(toEmbeddable(request.getToAddress()));
        delivery.setDeliveryState(DeliveryState.CREATED);
        return toDto(repository.save(delivery));
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateCost(OrderDto order) {
        validateOrder(order);
        DeliveryEntity delivery = order.getDeliveryId() == null
                ? null
                : repository.findById(order.getDeliveryId()).orElse(null);

        AddressDto fromAddress = delivery == null ? warehouseClient.getWarehouseAddress() : toDto(delivery.getFromAddress());
        AddressDto toAddress = delivery == null ? null : toDto(delivery.getToAddress());

        BigDecimal result = BASE_COST.add(BASE_COST.multiply(BigDecimal.valueOf(addressMultiplier(fromAddress))));
        if (Boolean.TRUE.equals(order.getFragile())) {
            result = result.add(result.multiply(FRAGILE_RATE));
        }
        result = result.add(BigDecimal.valueOf(defaultDouble(order.getDeliveryWeight())).multiply(WEIGHT_RATE));
        result = result.add(BigDecimal.valueOf(defaultDouble(order.getDeliveryVolume())).multiply(VOLUME_RATE));
        if (!sameStreet(fromAddress, toAddress)) {
            result = result.add(result.multiply(ADDRESS_RATE));
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void picked(String rawDeliveryId) {
        DeliveryEntity delivery = getDelivery(rawDeliveryId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        orderClient.assembly(delivery.getOrderId().toString());
        warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(delivery.getOrderId(), delivery.getDeliveryId()));
    }

    @Transactional
    public void successful(String rawDeliveryId) {
        DeliveryEntity delivery = getDelivery(rawDeliveryId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        orderClient.delivery(delivery.getOrderId().toString());
    }

    @Transactional
    public void failed(String rawDeliveryId) {
        DeliveryEntity delivery = getDelivery(rawDeliveryId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        orderClient.deliveryFailed(delivery.getOrderId().toString());
    }

    private void validateOrder(OrderDto order) {
        if (order == null || order.getOrderId() == null) {
            throw new DeliveryException("Order id is required", HttpStatus.BAD_REQUEST);
        }
    }

    private int addressMultiplier(AddressDto address) {
        String addressText = String.join(" ",
                value(address.getCountry()), value(address.getCity()), value(address.getStreet()),
                value(address.getHouse()), value(address.getFlat()));
        return addressText.contains("ADDRESS_2") ? 2 : 1;
    }

    private boolean sameStreet(AddressDto fromAddress, AddressDto toAddress) {
        return toAddress != null
                && StringUtils.hasText(fromAddress.getStreet())
                && fromAddress.getStreet().equals(toAddress.getStreet());
    }

    private DeliveryEntity getDelivery(String rawDeliveryId) {
        UUID deliveryId = parseId(rawDeliveryId);
        return repository.findById(deliveryId).orElseThrow(() ->
                new DeliveryException("Delivery " + deliveryId + " is not found", HttpStatus.NOT_FOUND));
    }

    private UUID parseId(String rawId) {
        try {
            return UUID.fromString(rawId.replace("\"", "").trim());
        } catch (RuntimeException exception) {
            throw new DeliveryException("Invalid delivery id: " + rawId, HttpStatus.BAD_REQUEST);
        }
    }

    private double defaultDouble(Double value) {
        return value == null ? 0 : value;
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private AddressEmbeddable toEmbeddable(AddressDto dto) {
        AddressEmbeddable address = new AddressEmbeddable();
        if (dto != null) {
            address.setCountry(dto.getCountry());
            address.setCity(dto.getCity());
            address.setStreet(dto.getStreet());
            address.setHouse(dto.getHouse());
            address.setFlat(dto.getFlat());
        }
        return address;
    }

    private AddressDto toDto(AddressEmbeddable address) {
        return new AddressDto(address.getCountry(), address.getCity(), address.getStreet(),
                address.getHouse(), address.getFlat());
    }

    private DeliveryDto toDto(DeliveryEntity delivery) {
        return new DeliveryDto(delivery.getDeliveryId(), toDto(delivery.getFromAddress()),
                toDto(delivery.getToAddress()), delivery.getOrderId(), delivery.getDeliveryState());
    }
}
