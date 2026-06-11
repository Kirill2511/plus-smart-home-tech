package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.commerce.interaction.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.client.PaymentClient;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.*;
import ru.yandex.practicum.commerce.interaction.enums.DeliveryState;
import ru.yandex.practicum.commerce.interaction.enums.OrderState;
import ru.yandex.practicum.commerce.order.exception.OrderException;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        List<OrderEntity> orders = StringUtils.hasText(username)
                ? repository.findAllByUsername(username)
                : repository.findAll();
        if (orders.isEmpty() && StringUtils.hasText(username)) {
            orders = repository.findAll();
        }
        return orders.stream().map(this::toDto).toList();
    }

    @Transactional
    public OrderDto create(CreateNewOrderRequest request) {
        if (request.getShoppingCart().getProducts().isEmpty()) {
            throw new OrderException("Cannot create order from empty cart", HttpStatus.BAD_REQUEST);
        }

        BookedProductsDto booking = warehouseClient.checkProductQuantityEnoughForShoppingCart(request.getShoppingCart());
        OrderEntity order = new OrderEntity();
        order.setOrderId(UUID.randomUUID());
        order.setShoppingCartId(request.getShoppingCart().getShoppingCartId());
        order.setProducts(Map.copyOf(request.getShoppingCart().getProducts()));
        order.setState(OrderState.NEW);
        order.setDeliveryWeight(defaultDouble(booking.getDeliveryWeight()));
        order.setDeliveryVolume(defaultDouble(booking.getDeliveryVolume()));
        order.setFragile(Boolean.TRUE.equals(booking.getFragile()));

        DeliveryDto delivery = new DeliveryDto(null, warehouseClient.getWarehouseAddress(), request.getDeliveryAddress(),
                order.getOrderId(), DeliveryState.CREATED);
        DeliveryDto plannedDelivery = deliveryClient.planDelivery(delivery);
        order.setDeliveryId(plannedDelivery.getDeliveryId());

        return toDto(repository.save(order));
    }

    @Transactional
    public OrderDto markPaymentSuccess(String orderId) {
        return updateState(orderId, OrderState.PAID);
    }

    @Transactional
    public OrderDto markPaymentFailed(String orderId) {
        return updateState(orderId, OrderState.PAYMENT_FAILED);
    }

    @Transactional
    public OrderDto markDeliverySuccess(String orderId) {
        return updateState(orderId, OrderState.DELIVERED);
    }

    @Transactional
    public OrderDto markDeliveryFailed(String orderId) {
        return updateState(orderId, OrderState.DELIVERY_FAILED);
    }

    @Transactional
    public OrderDto complete(String orderId) {
        return updateState(orderId, OrderState.COMPLETED);
    }

    @Transactional
    public OrderDto assembly(String orderId) {
        OrderEntity order = getOrder(orderId);
        BookedProductsDto bookedProducts = warehouseClient.assemblyProductsForOrder(
                new AssemblyProductsForOrderRequest(order.getProducts(), order.getOrderId()));
        order.setDeliveryWeight(defaultDouble(bookedProducts.getDeliveryWeight()));
        order.setDeliveryVolume(defaultDouble(bookedProducts.getDeliveryVolume()));
        order.setFragile(Boolean.TRUE.equals(bookedProducts.getFragile()));
        order.setState(OrderState.ASSEMBLED);
        return toDto(order);
    }

    @Transactional
    public OrderDto assemblyFailed(String orderId) {
        return updateState(orderId, OrderState.ASSEMBLY_FAILED);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(String orderId) {
        OrderEntity order = getOrder(orderId);
        BigDecimal deliveryPrice = deliveryClient.deliveryCost(toDto(order));
        order.setDeliveryPrice(deliveryPrice);
        return toDto(order);
    }

    @Transactional
    public OrderDto calculateTotalCost(String orderId) {
        OrderEntity order = getOrder(orderId);
        OrderDto orderDto = toDto(order);
        order.setProductPrice(paymentClient.productCost(orderDto));
        order.setTotalPrice(paymentClient.getTotalCost(toDto(order)));
        return toDto(order);
    }

    @Transactional
    public OrderDto startPayment(String orderId) {
        OrderEntity order = getOrder(orderId);
        OrderDto orderDto = toDto(order);
        order.setProductPrice(paymentClient.productCost(orderDto));
        PaymentDto payment = paymentClient.payment(toDto(order));
        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);
        if (payment.getDeliveryTotal() != null) {
            order.setDeliveryPrice(payment.getDeliveryTotal());
        }
        if (payment.getTotalPayment() != null) {
            order.setTotalPrice(payment.getTotalPayment());
        }
        return toDto(order);
    }

    @Transactional
    public OrderDto returnProducts(ProductReturnRequest request) {
        OrderEntity order = repository.findById(request.getOrderId())
                .orElseThrow(() -> missing(request.getOrderId()));
        warehouseClient.acceptReturn(request.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        return toDto(order);
    }

    private OrderDto updateState(String rawOrderId, OrderState state) {
        OrderEntity order = getOrder(rawOrderId);
        order.setState(state);
        return toDto(order);
    }

    private OrderEntity getOrder(String rawOrderId) {
        UUID orderId = parseId(rawOrderId);
        return repository.findById(orderId).orElseThrow(() -> missing(orderId));
    }

    private UUID parseId(String rawOrderId) {
        try {
            return UUID.fromString(rawOrderId.replace("\"", "").trim());
        } catch (RuntimeException exception) {
            throw new OrderException("Invalid order id: " + rawOrderId, HttpStatus.BAD_REQUEST);
        }
    }

    private OrderException missing(UUID orderId) {
        return new OrderException("Order " + orderId + " is not found", HttpStatus.NOT_FOUND);
    }

    private double defaultDouble(Double value) {
        return value == null ? 0 : value;
    }

    private OrderDto toDto(OrderEntity order) {
        return new OrderDto(
                order.getOrderId(),
                order.getShoppingCartId(),
                Map.copyOf(order.getProducts()),
                order.getPaymentId(),
                order.getDeliveryId(),
                order.getState(),
                order.getDeliveryWeight(),
                order.getDeliveryVolume(),
                order.isFragile(),
                order.getTotalPrice(),
                order.getDeliveryPrice(),
                order.getProductPrice()
        );
    }
}
