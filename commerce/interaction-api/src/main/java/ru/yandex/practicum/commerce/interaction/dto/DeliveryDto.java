package ru.yandex.practicum.commerce.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.enums.DeliveryState;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {
    private UUID deliveryId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private UUID orderId;
    private DeliveryState deliveryState;
}
