package ru.yandex.practicum.commerce.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.interaction.enums.ProductCategory;
import ru.yandex.practicum.commerce.interaction.enums.ProductState;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.store.model.ProductEntity;
import ru.yandex.practicum.commerce.store.repository.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory category, int page, int size, List<String> sort) {
        return repository.findByProductCategoryAndProductState(category, ProductState.ACTIVE,
                PageRequest.of(page, size, parseSort(sort))).map(this::toDto);
    }

    @Transactional
    public ProductDto create(ProductDto product) {
        ProductEntity entity = toEntity(product);
        entity.setProductId(UUID.randomUUID());
        return toDto(repository.save(entity));
    }

    @Transactional
    public ProductDto update(ProductDto product) {
        if (product.getProductId() == null || !repository.existsById(product.getProductId())) {
            throw notFound(product.getProductId());
        }
        return toDto(repository.save(toEntity(product)));
    }

    @Transactional
    public boolean deactivate(UUID productId) {
        ProductEntity entity = repository.findById(productId).orElseThrow(() -> notFound(productId));
        entity.setProductState(ProductState.DEACTIVATE);
        return true;
    }

    @Transactional
    public boolean setQuantityState(SetProductQuantityStateRequest request) {
        ProductEntity entity = repository.findById(request.getProductId())
                .orElseThrow(() -> notFound(request.getProductId()));
        entity.setQuantityState(request.getQuantityState());
        return true;
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        return repository.findById(productId).map(this::toDto).orElseThrow(() -> notFound(productId));
    }

    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sort.stream()
                .map(value -> value.split(","))
                .map(parts -> parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                        ? Sort.Order.desc(parts[0])
                        : Sort.Order.asc(parts[0]))
                .toList();
        return Sort.by(orders);
    }

    private ProductNotFoundException notFound(UUID productId) {
        return new ProductNotFoundException("Product " + productId + " not found");
    }

    private ProductDto toDto(ProductEntity entity) {
        return new ProductDto(entity.getProductId(), entity.getProductName(), entity.getDescription(),
                entity.getImageSrc(), entity.getQuantityState(), entity.getProductState(),
                entity.getProductCategory(), entity.getPrice());
    }

    private ProductEntity toEntity(ProductDto dto) {
        ProductEntity entity = new ProductEntity();
        entity.setProductId(dto.getProductId());
        entity.setProductName(dto.getProductName());
        entity.setDescription(dto.getDescription());
        entity.setImageSrc(dto.getImageSrc());
        entity.setQuantityState(dto.getQuantityState());
        entity.setProductState(dto.getProductState());
        entity.setProductCategory(dto.getProductCategory());
        entity.setPrice(dto.getPrice());
        return entity;
    }
}
