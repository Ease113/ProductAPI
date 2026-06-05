package com.inventory.service;

import com.inventory.dto.request.ProductCreateRequest;
import com.inventory.dto.request.ProductUpdateRequest;
import com.inventory.dto.response.ProductResponse;
import com.inventory.entity.Product;
import com.inventory.exception.BusinessException;
import com.inventory.exception.ErrorCode;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findAll() {
        Product product = Product.builder()
                .name("Test Product")
                .sku("TESTSKU123")
                .price(BigDecimal.valueOf(10000))
                .category("Test Category")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        Page<ProductResponse> response = productService.findAll(pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).name()).isEqualTo("Test Product");
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(productRepository).findAll(pageable);
    }

    @Test
    void findAllWithInvalidSort() {
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("string"));

        assertThatThrownBy(() -> productService.findAll(pageable))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    /*
    * 제품 생성
     */
    @Test
    void createProduct() {

        ProductCreateRequest productCreateRequest = new ProductCreateRequest(
                "Test Product",
                "TESTSKU123",
                BigDecimal.valueOf(10000),
                "Test Category"
        );

        Product product = Product.builder()
                .name(productCreateRequest.name())
                .sku(productCreateRequest.sku())
                .price(productCreateRequest.price())
                .category(productCreateRequest.category())
                .build();
        // 이미 등록된 SKU가 존재하지 않는다 가정
        when(productRepository.existsBySku(productCreateRequest.sku())).thenReturn(false);
        // 상품 저장 시 저장된 상품 객체 반환, Product 타입이면 어느것이든 상관없음
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.create(productCreateRequest);

        assertThat(response.name()).isEqualTo("Test Product");
        assertThat(response.sku()).isEqualTo("TESTSKU123");
        assertThat(response.price()).isEqualTo(BigDecimal.valueOf(10000));
        assertThat(response.category()).isEqualTo("Test Category");
    }

    /*
    * SKU 중복 테스트
     */
    @Test
    void skuExists() {
        ProductCreateRequest productCreateRequest = new ProductCreateRequest(
                "Test Product2",
                "TESTSKU123",
                BigDecimal.valueOf(10000),
                "Test Category"
        );

        when(productRepository.existsBySku(productCreateRequest.sku())).thenReturn(true);
        assertThatThrownBy(() -> productService.create(productCreateRequest))
                .isInstanceOf(BusinessException.class);
        // save가 호출되지 않았음을 확인
        verify(productRepository, never()).save(any(Product.class));
    }

    /*
    * 제품 업데이트 테스트
     */
    @Test
    void updateProduct() {
        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(
                "Updated Product",
                BigDecimal.valueOf(15000),
                "Updated Category"
        );
        // 업데이트 대상 제품정보가 이미 존재한다고 가정
        Product existingProduct = Product.builder()
                .name("Existing Product")
                .sku("EXISTENT123")
                .price(BigDecimal.valueOf(12000))
                .category("Existing Category")
                .build();

        Long id = 1L;
        // Optional 안에 existingProduct 객체가 존재한다고 가정
        when(productRepository.findById(id)).thenReturn(Optional.of(existingProduct));

        ProductResponse response = productService.update(id, productUpdateRequest);

        assertThat(response.name()).isEqualTo("Updated Product");
        assertThat(response.sku()).isEqualTo("EXISTENT123");
        assertThat(response.price()).isEqualTo(BigDecimal.valueOf(15000));
        assertThat(response.category()).isEqualTo("Updated Category");
    }

    /*
    * 제품 id가 존재하지 않을 경우 업데이트 시도 테스트
     */
    @Test
    void updateProductWithNonExistentId() {
        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(
                "Updated Product",
                BigDecimal.valueOf(15000),
                "Updated Category"
        );
        Long id = 1L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(id, productUpdateRequest))
                .isInstanceOf(BusinessException.class);
    }

    /*
    * 제품 삭제 테스트
     */
    @Test
    void deleteProduct() {
        Long id = 1L;

        when(productRepository.existsById(id)).thenReturn(true);

        productService.delete(id);
        verify(productRepository).deleteById(id);

    }

    /*
    * 제품 id가 존재하지 않을 경우 삭제 시도 테스트
     */
    @Test
    void deleteProductWithNonExistentId() {
        Long id = 1L;

        when(productRepository.existsById(id)).thenReturn(false);

        // 존재하지 않는 id에 대하여 삭제 시도시 예외 발생
        assertThatThrownBy(() -> productService.delete(id)).isInstanceOf(BusinessException.class);
        // 실제로 delete가 실행되지 않았음을 확인
        verify(productRepository, never()).deleteById(id);
    }
}
