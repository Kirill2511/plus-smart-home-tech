package ru.yandex.practicum.commerce.interaction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewProductInWarehouseRequest {
    @NotNull
    private UUID productId;

    private Boolean fragile = false;

    @Valid
    @NotNull
    private DimensionDto dimension;

    @NotNull
    @Min(1)
    private Double weight;
}
