package ru.yandex.practicum.commerce.interaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.enums.ProductCategory;
import ru.yandex.practicum.commerce.interaction.enums.ProductState;
import ru.yandex.practicum.commerce.interaction.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID productId;

    @NotBlank
    private String productName;

    @NotBlank
    @Size(max = 2000)
    private String description;

    private String imageSrc;

    @NotNull
    private QuantityState quantityState;

    @NotNull
    private ProductState productState;

    @NotNull
    private ProductCategory productCategory;

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal price;
}
