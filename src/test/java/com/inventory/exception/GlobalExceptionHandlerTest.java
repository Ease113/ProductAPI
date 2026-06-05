package com.inventory.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 도메인 컨트롤러가 아직 없으므로, 예외만 던지는 Stub 컨트롤러를 세워
 * GlobalExceptionHandler가 BusinessException을 표준 봉투(404/C002)로 변환하는지 검증한다.
 *
 * <p>standaloneSetup으로 컨트롤러 + ControllerAdvice만 올린다. Spring 컨텍스트(DataSource·
 * Security 등)를 띄우지 않아 빠르고, "예외 → 봉투" 변환이라는 검증 대상에 정확히 집중된다.
 */
class GlobalExceptionHandlerTest {

    @RestController
    static class StubController {
        @GetMapping("/_test/notfound")
        void notFound() {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new StubController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void businessException_maps_status_and_envelope() throws Exception {
        mvc.perform(get("/_test/notfound"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C002"));
    }
}
