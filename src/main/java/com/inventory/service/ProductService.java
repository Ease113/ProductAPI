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

    /**
     * 페이지 조건에 맞는 상품 목록을 조회하고 API용 페이지 DTO로 변환한다.
     * 동일한 페이지 조건의 결과는 Redis 캐시에 저장된다.
     *
     * @param pageable 페이지 번호, 크기, 정렬 조건
     * @return 상품 목록과 페이지 정보
     * @throws BusinessException 허용되지 않은 필드로 정렬을 요청한 경우
     */
    @Cacheable(cacheNames = CacheNames.PRODUCTS,
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public PageResponse<ProductResponse> findAll(Pageable pageable) {
        validateSort(pageable.getSort());
        Page<ProductResponse> page = productRepository.findAll(pageable)
                .map(ProductResponse::from);
        return PageResponse.from(page);
    }

    /**
     * 요청된 정렬 속성이 상품에서 허용한 필드인지 검증한다.
     *
     * @param sort 검증할 정렬 조건
     * @throws BusinessException 허용되지 않은 정렬 속성이 포함된 경우
     */
    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }
    }

    /**
     * ID로 상품을 조회한다. 조회 결과는 상품 ID를 키로 Redis에 캐시된다.
     *
     * @param id 조회할 상품 ID
     * @return 조회된 상품 정보
     * @throws BusinessException 상품이 존재하지 않는 경우
     */
    @Cacheable(cacheNames = CacheNames.PRODUCT_DETAIL, key = "#id")
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }

    /**
     * SKU 중복을 확인하고 새로운 상품을 저장한다.
     * 성공하면 기존 상품 목록 캐시를 모두 제거한다.
     *
     * @param request 상품 생성 요청
     * @return 생성된 상품 정보
     * @throws BusinessException 동일한 SKU가 이미 존재하는 경우
     */
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

    /**
     * 기존 상품의 변경 가능한 정보를 수정하고 관련 목록·상세 캐시를 제거한다.
     *
     * @param id 수정할 상품 ID
     * @param request 상품 수정 요청
     * @return 수정된 상품 정보
     * @throws BusinessException 상품이 존재하지 않는 경우
     */
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

    /**
     * 상품을 삭제하고 관련 목록·상세 캐시를 제거한다.
     *
     * @param id 삭제할 상품 ID
     * @throws BusinessException 상품이 존재하지 않는 경우
     */
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
