package ru.yandex.practicum.commerce.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.enums.OrderState;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID orderId;
    private UUID shoppingCartId;
    private Map<UUID, Long> products = new HashMap<>();
    private UUID paymentId;
    private UUID deliveryId;
    private OrderState state;
    private Double deliveryWeight;
    private Double deliveryVolume;
    private Boolean fragile;
    private BigDecimal totalPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productPrice;
}
