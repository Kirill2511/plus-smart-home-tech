package ru.yandex.practicum.commerce.interaction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToWarehouseRequest {
    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Long quantity;
}
