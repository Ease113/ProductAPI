# Phase 1 코드 가이드 — 공통 기반 (Common Foundation)

> 이 문서는 **구현된 코드를 따라 읽으며 "무엇을·왜 이 순서로" 만들었는지** 이해하기 위한 안내서다.
> 설계 의도는 [design/phase1-common-foundation.md](../design/phase1-common-foundation.md), 전체 지도는 [ROADMAP.md](../ROADMAP.md) 참고.
> Created: 2026-06-02

## 한눈에 보기

Phase 1은 도메인 기능(상품·재고·인증)을 만들기 **전에**, 그 위에 모든 단계가 올라설 *공통 토대*를 세운 단계다.
"돌아가는 기능"은 없지만, 이후 모든 컨트롤러가 같은 응답 형태·같은 예외 처리·같은 보안 통과 경로를 공유하게 된다.

```
요청 ──► [SecurityFilterChain: 임시 permitAll] ──► (Controller: 도메인 단계)
                                                     │ 정상 → ApiResponse.success(data)
                                                     │ 예외 → GlobalExceptionHandler ─► ApiResponse.error(code, msg)
                                                     ▼
                                              [Repository(JpaRepository)] ──► MySQL
        Swagger(OpenApiConfig) 는 SecurityFilterChain을 통과해 /swagger-ui.html 로 노출
```

## 파일별 역할 (코드 읽는 순서 = 의존성 순서)

아래 순서대로 읽으면 뒤 파일이 앞 파일에 어떻게 기대는지 자연스럽게 보인다.

### 1. Repository 4개 — `repository/*Repository.java`
- **무엇**: `JpaRepository<Entity, Long>`를 상속한 빈 인터페이스 4개(Product/Inventory/StockLog/Member).
- **왜 먼저**: 데이터 접근의 최하단 계층. 다른 코드에 의존하지 않아 가장 먼저 둘 수 있고, 엔티티(Phase 0)와 1:1로 대응해 만들기 쉽다.
- **지금 비어 있는 이유**: 커스텀 조회(`findByEmail`, 비관적 락 등)는 *그 기능이 필요한 단계*에서 추가한다. 미리 만들지 않는다(투기적 코드 금지).

### 2. `ApiResponse<T>` — `dto/response/ApiResponse.java`
- **무엇**: 성공·실패를 모두 감싸는 응답 봉투 `{ success, data, error }` (record).
- **왜 이 자리**: 정상 응답과 에러 응답 **둘 다** 이 타입을 쓴다. 그래서 예외 처리(4번)보다 먼저 존재해야 한다 — 핸들러가 `ApiResponse.error(...)`를 호출한다.
- **설계 근거**: 클라이언트가 단일 규약으로 분기하도록(Decision 1). 성공은 raw DTO, 실패는 ProblemDetail로 가르는 대안 대비 학습·설명 비용이 낮다.

### 3. `ErrorCode` + `BusinessException` — `exception/ErrorCode.java`, `exception/BusinessException.java`
- **무엇**: 오류 카탈로그(enum: status+코드+메시지) 하나와, 그 코드를 들고 다니는 단일 예외 하나.
- **왜 이 자리**: 예외를 *표현*하는 재료. 핸들러(4번)가 이 둘을 읽어 응답으로 바꾸므로 핸들러보다 먼저다.
- **설계 근거**: 오류를 종류별 예외 클래스로 흩뿌리지 않고 enum 한 곳에 모은다(Decision 2). 새 오류 = 상수 한 줄 추가.
- **읽기 팁**: `ErrorCode`의 코드 문자열(C001…)은 응답에 노출되는 **안정적 키**다. 한 번 부여하면 바꾸지 않는다.

### 4. `GlobalExceptionHandler` — `exception/GlobalExceptionHandler.java`
- **무엇**: `@RestControllerAdvice`. 세 갈래 예외를 표준 봉투로 변환한다.
  - `BusinessException` → ErrorCode의 status/코드 (예: 404 C002)
  - `@Valid` 실패(`MethodArgumentNotValidException`) → 400 C001 + 첫 필드 오류 요약
  - 미처리 `Exception` → 500 C999 (상세는 로그만, 응답엔 일반 메시지)
- **왜 2·3 다음**: `ApiResponse`(2)와 `ErrorCode`/`BusinessException`(3)에 **모두 의존**한다. 셋 중 가장 마지막에 와야 컴파일된다.
- **읽기 팁**: 핸들러 메서드는 구체 타입 → 일반 타입(`Exception`) 순으로 매칭된다. 그래서 미처리 핸들러가 다른 걸 잡아먹지 않는다.

### 5. `OpenApiConfig` — `config/OpenApiConfig.java`
- **무엇**: Swagger UI 상단에 표시될 문서 메타데이터(title/version)만 정의.
- **왜 독립적**: 위 1~4와 의존 관계가 없다(병렬로 만들어도 됨). 엔드포인트 목록은 Phase 2부터 컨트롤러가 생기면 자동 수집된다.

### 6. `SecurityConfig` (⚠️ 임시) — `security/SecurityConfig.java`
- **무엇**: 모든 요청을 통과시키는 임시 `SecurityFilterChain`(permitAll + csrf disable).
- **왜 필요**: `spring-boot-starter-security`가 클래스패스에 있으면 **기본값이 전체 401**이라 Swagger조차 안 열린다. 이 한 줄짜리 정책으로 Phase 1~3 동안 개발·문서 확인을 가능케 한다.
- **왜 "지우지 않고 임시로"**: 의존성을 뺐다 넣으면 Phase 4에서 빌드가 출렁인다. 설정 Bean *한 파일*만 두면 Phase 4에서 **이 파일만** 정식 JWT 규칙으로 교체 → 변경 국소화(Decision 3).
- **읽기 팁**: 파일 상단 ⚠️ 주석이 "이건 임시다"라는 표식. Phase 4 진입 시 가장 먼저 열어야 할 파일.

### 7. `GlobalExceptionHandlerTest` — `src/test/.../exception/GlobalExceptionHandlerTest.java`
- **무엇**: 예외만 던지는 Stub 컨트롤러를 세워 "BusinessException → 404/C002 봉투" 변환을 검증.
- **왜 마지막**: 검증 대상(2~6, 특히 4)이 다 있어야 의미가 있다.
- **구현 노트(설계와 달라진 점)**: 설계 초안은 `@WebMvcTest + @Import(SecurityConfig)`였으나, 슬라이스가 컨트롤러 매핑을 잡지 못해(요청이 정적 리소스로 falling through → 500) **`MockMvcBuilders.standaloneSetup`**으로 변경했다. 컨트롤러+ControllerAdvice만 올려 Spring 컨텍스트(DataSource/Security)를 띄우지 않으므로 더 빠르고, "예외→봉투"라는 검증 대상에 정확히 집중된다. (보안은 이 테스트의 관심사가 아니다.)

## "왜 이 순서인가"를 한 문장으로

> **아래에서 위로, 의존되는 것부터.** 데이터 접근(Repository) → 응답 형태(ApiResponse) → 오류 재료(ErrorCode/Exception) → 오류 변환기(Handler) 순으로 쌓고, 그 흐름과 무관한 문서(OpenApi)·보안(Security)은 옆에 끼워 넣은 뒤, 마지막에 테스트로 묶었다.

코드 의존 그래프로 보면:

```
ApiResponse ──┐
              ├─► GlobalExceptionHandler ──► (Test)
ErrorCode ────┤
BusinessException ┘
Repository(독립)        OpenApiConfig(독립)        SecurityConfig(독립) ──► (Test가 참조 X, 현재)
```

## 검증 방법 (Definition of Done)

1. **빌드+테스트**: `./gradlew build` — 컴파일 + 슬라이스 테스트 그린. *(완료 확인됨: tests=1, failures=0)*
2. **인프라**: `docker compose up -d` (MySQL + Redis)
3. **앱 기동**: `./gradlew bootRun` — Flyway 마이그레이션 + Hibernate `validate` 통과, Repository 4개 Bean 등록 오류 없음
4. **Swagger 스모크 (= DoD)**: PowerShell에서
   ```powershell
   (Invoke-WebRequest http://localhost:8080/swagger-ui/index.html -UseBasicParsing).StatusCode  # → 200
   ```
5. **(선택) 메타데이터**: `http://localhost:8080/v3/api-docs` 에 title="재고관리 API", version="v0.1"

## 다음 단계로 가는 다리 (Phase 2 예고)

이 토대 위에서 Phase 2(상품 CRUD)는:
- `ProductRepository`에 필요한 조회 메서드를 더하고,
- `ProductController`가 결과를 **`ApiResponse.success`**로 감싸며,
- 없는 상품은 **`throw new BusinessException(RESOURCE_NOT_FOUND)`** → `GlobalExceptionHandler`가 404 봉투로 변환,
- 새 엔드포인트는 임시 `SecurityConfig`의 permitAll 덕에 인증 없이 바로 호출 가능.

즉 Phase 1의 5개 부품이 Phase 2에서 처음으로 "함께" 돌아가는 것을 보게 된다.
