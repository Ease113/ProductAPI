package com.inventory.controller;

import com.inventory.dto.request.ProductCreateRequest;
import com.inventory.dto.request.ProductUpdateRequest;
import com.inventory.dto.response.ApiResponse;
import com.inventory.dto.response.PageResponse;
import com.inventory.dto.response.ProductResponse;
import com.inventory.service.ProductService;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product API", description = "제품 관리 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Product 전체 조회 -> API 응답의 일관성을 위해 PageResponse 래퍼 사용
    @Operation(summary = "제품 전체 조회", description = "페이지네이션을 지원하는 제품 전체 조회 API")
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> findAll(@ParameterObject Pageable pageable) {
        return ApiResponse.success(PageResponse.from(productService.findAll(pageable)));
    }
    // Product 전체 조회
//    @GetMapping
//    public ApiResponse<Page<ProductResponse>> findAll(Pageable pageable) {
//        return ApiResponse.success(productService.findAll(pageable));
//    }

    // Product ID로 단일 조회
    @Operation(summary = "제품 단일 조회", description = "ID로 제품을 조회하는 API")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(productService.findById(id));
    }

    @Operation(summary = "제품 생성", description = "새로운 제품을 생성하는 API")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductCreateRequest request
            ) {
        ProductResponse product = productService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(product));
    }

    @Operation(summary = "제품 수정", description = "ID로 제품을 수정하는 API")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ApiResponse.success(productService.update(id, request));
    }

    @Operation(summary = "제품 삭제", description = "ID로 제품을 삭제하는 API")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success(null);
    }
}
