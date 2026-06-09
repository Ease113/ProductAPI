package com.inventory.service;

import com.inventory.common.cache.CacheNames;
import com.inventory.dto.request.ProductCreateRequest;
import com.inventory.dto.request.ProductUpdateRequest;
import com.inventory.dto.response.PageResponse;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.exception.ErrorCode;
import com.inventory.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
            "id",
            "name",
            "sku",
            "price",
            "category",
            "createdAt"
    );

    private final ProductRepository productRepository;

    @Cacheable(cacheNames = CacheNames.PRODUCTS,
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public PageResponse<ProductResponse> findAll(Pageable pageable) {
        validateSort(pageable.getSort());
        Page<ProductResponse> page = productRepository.findAll(pageable)
                .map(ProductResponse::from);
        return PageResponse.from(page);
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }
    }
    @Cacheable(cacheNames = CacheNames.PRODUCT_DETAIL, key = "#id")
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }
    @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true)
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        Product product = Product.builder()
                .name(request.name())
                .sku(request.sku())
                .price(request.price())
                .category(request.category())
                .build();
        return ProductResponse.from(productRepository.save(product));
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_DETAIL, key = "#id")
    })
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        product.update(
                request.name(),
                request.price(),
                request.category()
        );
        return ProductResponse.from(product);
    }
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_DETAIL, key = "#id")
    })
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }
}
