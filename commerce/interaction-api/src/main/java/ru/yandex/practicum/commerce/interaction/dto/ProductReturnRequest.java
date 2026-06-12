package ru.yandex.practicum.commerce.interaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    private Map<UUID, Long> products = new HashMap<>();
}
