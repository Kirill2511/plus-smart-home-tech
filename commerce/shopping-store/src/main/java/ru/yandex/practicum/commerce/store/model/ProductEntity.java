package ru.yandex.practicum.commerce.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.commerce.interaction.enums.ProductCategory;
import ru.yandex.practicum.commerce.interaction.enums.ProductState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductEntity {
    @Id
    private UUID productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, length = 2000)
    private String description;

    private String imageSrc;

    @Column(nullable = false)
    private String quantityState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductState productState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory productCategory;

    @Column(nullable = false)
    private BigDecimal price;
}
