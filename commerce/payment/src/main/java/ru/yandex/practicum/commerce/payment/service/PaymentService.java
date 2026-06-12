package ru.yandex.practicum.commerce.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.exception.PaymentException;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;
import ru.yandex.practicum.commerce.payment.model.PaymentState;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final PaymentRepository repository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Transactional
    public PaymentDto createPayment(OrderDto order) {
        validateOrder(order);
        BigDecimal productTotal = productCost(order);
        BigDecimal deliveryTotal = requireDeliveryPrice(order);
        BigDecimal feeTotal = calculateFee(productTotal);
        BigDecimal totalPayment = productTotal.add(feeTotal).add(deliveryTotal);

        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(order.getOrderId());
        payment.setProductTotal(productTotal);
        payment.setDeliveryTotal(deliveryTotal);
        payment.setFeeTotal(feeTotal);
        payment.setTotalPayment(totalPayment);
        payment.setState(PaymentState.PENDING);

        return toDto(repository.save(payment));
    }

    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto order) {
        validateOrder(order);
        return order.getProducts().entrySet().stream()
                .map(entry -> shoppingStoreClient.getProduct(entry.getKey()).getPrice()
                        .multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalCost(OrderDto order) {
        BigDecimal productTotal = productCost(order);
        return productTotal
                .add(calculateFee(productTotal))
                .add(requireDeliveryPrice(order))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void success(String rawPaymentId) {
        PaymentEntity payment = getPayment(rawPaymentId);
        payment.setState(PaymentState.SUCCESS);
        orderClient.payment(payment.getOrderId().toString());
    }

    @Transactional
    public void failed(String rawPaymentId) {
        PaymentEntity payment = getPayment(rawPaymentId);
        payment.setState(PaymentState.FAILED);
        orderClient.paymentFailed(payment.getOrderId().toString());
    }

    private void validateOrder(OrderDto order) {
        if (order == null || order.getOrderId() == null) {
            throw new PaymentException("Order id is required", HttpStatus.BAD_REQUEST);
        }
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new PaymentException("Order products are required", HttpStatus.BAD_REQUEST);
        }
    }

    private BigDecimal requireDeliveryPrice(OrderDto order) {
        if (order.getDeliveryPrice() == null) {
            throw new PaymentException("Delivery price is required to calculate payment", HttpStatus.BAD_REQUEST);
        }
        return order.getDeliveryPrice();
    }

    private BigDecimal calculateFee(BigDecimal productTotal) {
        return productTotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private PaymentEntity getPayment(String rawPaymentId) {
        UUID paymentId = parseId(rawPaymentId);
        return repository.findById(paymentId).orElseThrow(() ->
                new PaymentException("Payment " + paymentId + " is not found", HttpStatus.NOT_FOUND));
    }

    private UUID parseId(String rawPaymentId) {
        try {
            return UUID.fromString(rawPaymentId.replace("\"", "").trim());
        } catch (RuntimeException exception) {
            throw new PaymentException("Invalid payment id: " + rawPaymentId, HttpStatus.BAD_REQUEST);
        }
    }

    private PaymentDto toDto(PaymentEntity payment) {
        return new PaymentDto(payment.getPaymentId(), payment.getTotalPayment(),
                payment.getDeliveryTotal(), payment.getFeeTotal());
    }
}
