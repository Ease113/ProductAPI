package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.request.ProductCreateRequest;
import com.inventory.dto.request.ProductUpdateRequest;
import com.inventory.dto.response.PageResponse;
import com.inventory.dto.response.ProductResponse;
import com.inventory.service.ProductService;
import com.inventory.security.JwtTokenProvider;
import com.inventory.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /*
    * 제품 생성 테스트
     */
    @Test
    void createProduct() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest(
                "Test Product",
                "TESTSKU123",
                BigDecimal.valueOf(10000),
                "Test Category"
        );

        ProductResponse response = new ProductResponse(
                1L,
                "Test Product",
                "TESTSKU123",
                BigDecimal.valueOf(10000),
                "Test Category"
        );
        // 서비스 레이어의 create 메서드가 호출되면, 미리 정의된 response 객체를 반환하도록 설정
        when(productService.create(any(ProductCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.sku").value("TESTSKU123"))
                .andExpect(jsonPath("$.data.price").value(10000))
                .andExpect(jsonPath("$.data.category").value("Test Category"));
        
        // 서비스 레이어의 create 메서드가 제품 생성 요청 객체로 호출되었는지 검증
        verify(productService).create(any(ProductCreateRequest.class));
    }

    /*
    * 전체 조회 테스트
     */
    @Test
    void findAll() throws Exception {
        ProductResponse response = new ProductResponse(
                1L,
                "Test Product",
                "TESTSKU123",
                BigDecimal.valueOf(10000),
                "Test Category"
        );

        // 서비스 레이어의 findAll 메서드가 호출되면, 미리 정의된 response 객체를 포함하는 페이지를 반환하도록 설정
        PageResponse<ProductResponse> pageResponse = new PageResponse<>(
                List.of(response), 0, 10, 1, 1, true, true
        );
        given(productService.findAll(any(Pageable.class))).willReturn(pageResponse);
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.data.content[0].sku").value("TESTSKU123"))
                .andExpect(jsonPath("$.data.content[0].price").value(10000))
                .andExpect(jsonPath("$.data.content[0].category").value("Test Category"));

        // 서비스 레이어의 findAll 메서드가 호출되었는지 검증
        verify(productService).findAll(any(Pageable.class));
    }

    @Test
    void findById() throws Exception {
        ProductResponse response = new ProductResponse(
                1L,
                "Test Product",
                "TESTSKU123",
                BigDecimal.valueOf(10000),
                "Test Category"
        );
        // 서비스 레이어의 findById 메서드가 호출되면, 미리 정의된 response 객체를 반환하도록 설정
        when(productService.findById(1L)).thenReturn(response);
        // ID가 1인 제품을 조회하는 GET 요청을 시뮬레이션하고, 응답이 예상대로인지 검증
        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Test Product"))
                .andExpect(jsonPath("$.data.sku").value("TESTSKU123"))
                .andExpect(jsonPath("$.data.price").value(10000))
                .andExpect(jsonPath("$.data.category").value("Test Category"));

        // 서비스 레이어의 findById 메서드가 ID 1L로 호출되었는지 검증
        verify(productService).findById(1L);
    }

    @Test
    void updateProduct() throws Exception {
        // 업데이트 요청 객체 생성
        ProductUpdateRequest request = new ProductUpdateRequest(
                "Updated Product",
                BigDecimal.valueOf(15000),
                "Updated Category"
        );

        // 업데이트 후의 응답 객체 생성
        ProductResponse response = new ProductResponse(
                1L,
                "Updated Product",
                "TESTSKU123",
                BigDecimal.valueOf(15000),
                "Updated Category"
        );
        // 서비스 레이어의 update 메서드가 호출되면, 미리 정의된 response 객체를 반환하도록 설정
        when(productService.update(any(Long.class), any(ProductUpdateRequest.class))).thenReturn(response);

        // ID가 1인 제품을 업데이트하는 PUT 요청을 시뮬레이션하고, 응답이 예상대로인지 검증
        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Updated Product"))
                .andExpect(jsonPath("$.data.sku").value("TESTSKU123"))
                .andExpect(jsonPath("$.data.price").value(15000))
                .andExpect(jsonPath("$.data.category").value("Updated Category"));

        // 서비스 레이어의 update 메서드가 ID 1L와 업데이트 요청 객체로 호출되었는지 검증
        verify(productService).update(any(Long.class), any(ProductUpdateRequest.class));
    }

    @Test
    void deleteProduct() throws Exception {
        // ID가 1인 제품을 삭제하는 DELETE 요청을 시뮬레이션하고, 응답이 예상대로인지 검증
        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());

        // 서비스 레이어의 delete 메서드가 ID 1L로 호출되었는지 검증
        verify(productService).delete(1L);
    }
}
