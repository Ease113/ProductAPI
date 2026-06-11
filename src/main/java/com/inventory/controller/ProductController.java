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

    /**
     * 페이지 조건에 맞는 상품 목록을 조회한다.
     *
     * @param pageable 페이지 번호, 크기, 정렬 조건
     * @return 상품 목록과 페이지 정보가 담긴 공통 응답
     */
    @Operation(summary = "제품 전체 조회", description = "페이지네이션을 지원하는 제품 전체 조회 API")
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> findAll(@ParameterObject Pageable pageable) {
        return ApiResponse.success(productService.findAll(pageable));
    }

    /**
     * 상품 ID로 단일 상품을 조회한다.
     *
     * @param id 조회할 상품 ID
     * @return 조회된 상품 정보가 담긴 공통 응답
     */
    @Operation(summary = "제품 단일 조회", description = "ID로 제품을 조회하는 API")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findById(@PathVariable Long id) {
        return ApiResponse.success(productService.findById(id));
    }

    /**
     * 새로운 상품을 등록한다.
     *
     * @param request 검증된 상품 생성 요청
     * @return 생성된 상품 정보와 HTTP 201 응답
     */
    @Operation(summary = "제품 생성", description = "새로운 제품을 생성하는 API")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductCreateRequest request
            ) {
        ProductResponse product = productService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(product));
    }

    /**
     * 지정한 상품의 이름, 가격, 카테고리를 수정한다.
     *
     * @param id 수정할 상품 ID
     * @param request 검증된 상품 수정 요청
     * @return 수정된 상품 정보가 담긴 공통 응답
     */
    @Operation(summary = "제품 수정", description = "ID로 제품을 수정하는 API")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ApiResponse.success(productService.update(id, request));
    }

    /**
     * 지정한 상품을 삭제한다.
     *
     * @param id 삭제할 상품 ID
     * @return 성공 여부만 포함하는 공통 응답
     */
    @Operation(summary = "제품 삭제", description = "ID로 제품을 삭제하는 API")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.success(null);
    }
}
