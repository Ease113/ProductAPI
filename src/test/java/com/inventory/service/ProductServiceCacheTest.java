package com.inventory.service;

import com.inventory.common.cache.CacheNames;
import com.inventory.dto.request.ProductCreateRequest;
import com.inventory.dto.request.ProductUpdateRequest;
import com.inventory.dto.response.PageResponse;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ProductServiceCacheTest {

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void cacheClear() {
        cacheManager.getCache(CacheNames.PRODUCTS).clear();
        cacheManager.getCache(CacheNames.PRODUCT_DETAIL).clear();
    }
    // 테스트에서 사용할 Product 객체를 생성하는 헬퍼 메서드
    private Product createProduct(Long id, String name, String sku, BigDecimal price, String category) {
        Product product = Product.builder()
                .name(name)
                .sku(sku)
                .price(price)
                .category(category)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
    @Test
    @DisplayName("상품 상세 조회는 같은 id로 두 번 조회해도 Repository를 한 번만 호출한다")
    void findById_useCache() {
        Product product = createProduct(
                1L,
                "Keyboard",
                "SKU-001",
                BigDecimal.valueOf(10000),
                "DEVICE");

        given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));

        // 첫 번째 조회 - Repository에서 데이터를 가져와 캐시에 저장
        ProductResponse response1 = productService.findById(1L);
        // 두 번째 조회 - 캐시에서 데이터를 가져옴, Repository는 호출되지 않음
        ProductResponse response2 = productService.findById(1L);

        assertThat(response2).isEqualTo(response1);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("상품 목록 조회는 같은 페이지 요청으로 두 번 조회해도 Repository를 한 번만 호출한다")
    void findAll_useCache() {
        Product product = createProduct(
                1L,
                "Keyboard",
                "SKU-001",
                BigDecimal.valueOf(10000),
                "DEVICE");

        PageRequest pageRequest = PageRequest.of(0, 10);
        given(productRepository.findAll(pageRequest)).willReturn(new PageImpl<>(List.of(product), pageRequest, 1));

        PageResponse<ProductResponse> response1 = productService.findAll(pageRequest);
        PageResponse<ProductResponse> response2 = productService.findAll(pageRequest);

        assertThat(response2).isEqualTo(response1);
        assertThat(response2.content()).hasSize(1);
        assertThat(response2.content().get(0).name()).isEqualTo("Keyboard");
        verify(productRepository, times(1)).findAll(pageRequest);
    }

    @Test
    @DisplayName("상품 생성 후 캐시 삭제")
    void create_evictsProductsCache() {
        Product product = createProduct(
                1L,
                "Keyboard",
                "SKU-001",
                BigDecimal.valueOf(10000),
                "DEVICE");
        PageRequest pageRequest = PageRequest.of(0, 10);

        given(productRepository.findAll(pageRequest)).willReturn(new PageImpl<>(List.of(product), pageRequest, 1));

        productService.findAll(pageRequest);
        productService.findAll(pageRequest);

        verify(productRepository, times(1)).findAll(pageRequest);

        ProductCreateRequest createRequest = new ProductCreateRequest(
                "Mouse",
                "SKU-002",
                BigDecimal.valueOf(5000),
                "DEVICE"
        );

        Product savedProduct = createProduct(2L, "Mouse", "SKU-002", BigDecimal.valueOf(10000), "DEVICE");

        given(productRepository.existsBySku(createRequest.sku())).willReturn(false);
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        productService.create(createRequest);
        productService.findAll(pageRequest);
        // 상품 생성 후 기존 캐시 삭제 -> 새로운 캐시 등록으로 레포지토리가 총 2번 호출되어야 함
        verify(productRepository, times(2)).findAll(pageRequest);
    }
    @Test
    @DisplayName("상품 수정 후 캐시 삭제")
    void update_evictsProductsCache() {
        Product product = createProduct(
                1L,
                "Keyboard",
                "SKU-001",
                BigDecimal.valueOf(10000),
                "DEVICE"
        );
        PageRequest pageRequest = PageRequest.of(0, 10);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        productService.findById(1L);
        productService.findById(1L);
        verify(productRepository, times(1)).findById(1L);

        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                "Mechanical Keyboard",
                BigDecimal.valueOf(12000),
                "DEVICE"
        );
        clearInvocations(productRepository);
        productService.update(1L, updateRequest);
        verify(productRepository, times(1)).findById(1L);
    }


}
