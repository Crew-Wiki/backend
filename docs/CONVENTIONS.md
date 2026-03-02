# CrewWiki Backend 컨벤션

## 1. 패키지 구조
```
├── post
│   ├── controller
│   ├── dto
│   ├── repository
│   └── service
└── user
│   ├── controller
│   ├── dto
│   ├── repository
│   └── service
├── common
│   ├── config
│   ├── entity
│   ├── exception
│   ├── infrastructure
│   ├── log
│   └── utils
```

## 2. 테스트 코드
### 2.1 네이밍
- `@Nested`는 프로덕션 **메서드명**을 기준으로 작성한다.
- `@Nested` 클래스명은 **UpperCamel**로 표기한다.
- 테스트 메서드명 형식:
  - `프로덕션메서드_success_조건`
  - `프로덕션메서드_fail_조건`

예시
```java
@Nested
@DisplayName("로그인 기능")
class Login {
    @Test
    @DisplayName("유효한 정보로 로그인 성공")
    void login_success_byValidCredentials() { }

    @Test
    @DisplayName("유효하지 않은 정보로 로그인 실패")
    void login_fail_byInvalidCredentials() { }
}
```

### 2.2 BDD 패턴
```java
// given (불필요하면 생략)
// when
// then
```

### 2.3 Fixture
- 정적 팩토리 메서드 사용

예시
```java
CrewDocument doc = DocumentFixture.createDefaultCrewDocument();
```

### 2.4 테스트 환경 및 전략
- 인증/인가 필요 시 컨트롤러 테스트에서 `RestAssured` 사용
- 서비스 테스트는 통합 테스트로 작성 (`@SpringBootTest(WebEnvironment.MOCK/NONE)`)하여 톰캣을 띄우지 않음
- 필요 시 `assertSoftly`로 감싸기
- 결과 검증은 **상태 검증 우선**
- 서비스 테스트는 필수, 컨트롤러 테스트는 필요 시

## 3. 네이밍 규칙
- 메서드명: 동사로 시작, 엔티티명 미사용
  - 예: `memberService.findById(Long id)`
- Path Variable 명확화
  - 예: `/api/{documentId}`
- DTO: `Request`/`Response` 접미사 사용
  - 등록/수정은 `Register`/`Update` 가능
  - 예: `DocumentSearchResponse`, `DocumentUpdateRequest`

## 4. 객체지향 생활체조 (최대한 지킴)
1. 한 메서드 한 indent
2. `else` 금지
3. 원시값·문자열 포장
4. 한 줄에 점 하나
5. 축약 금지
6. 작은 엔티티
7. 인스턴스 변수 ≤ 2
8. 일급 컬렉션
9. Getter/Setter는 필요할 때만 사용

## 5. 예외 처리
- 에러 코드를 포함해 반환

## 6. 어노테이션 순서 (권장)
1. 로깅
2. 롬복
3. 스프링 메타
4. 스프링 컴포넌트 (가장 아래)

예시
```java
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "...")
public class SampleEntity {
    ...
}
```

## 7. 도메인 외부 노출 범위
- 서비스 외부에는 DTO만 노출

예시
```java
public ApiResponse<SuccessBody<DocumentResponse>> getByUuid(...) { }
```

## 8. API ↔ DTO
- 1:1 매핑
- 어드민/일반 API가 다르면 DTO도 분리

예시
- 어드민 문서 검색 → `AdminDocumentSearchResponse`
- 일반 문서 검색 → `DocumentSearchResponse`

## 9. 코드 스타일
- 클래스 선언 전 공백 한 줄
- 메서드 파라미터 2개 이상이면 개행
- 메서드 라인 수 제한 없음 (개행 규칙 우선)

## 10. 검증 위치
- 행동 검증: 서비스
- 상태 검증: 도메인
- 애매한 경우 개발 시 협의

## 11. import
- 와일드카드 금지
- IntelliJ 컨벤션 적용
- static import 허용

## 12. 기타
- `final`은 필드/상수에 사용 가능
- 정적 팩토리 메서드 사용 가능
- VO 미사용 (필요 시 도입)
- 상수는 `UPPER_SNAKE_CASE`
- 접근자/메서드 정렬: `public` → `protected` → `private`
  - 예외: getter는 맨 아래

## 13. DTO 정책 (업데이트)
- DTO는 `record`를 최대한 유지
- 불가피한 경우에만 `class` 사용 (예: `@ModelAttribute` 바인딩 등)

현재 적용된 공용 네이밍
- `PagedResponse`
- `PagingRequest`
- `TokenInfoResponse`
- `AuthTokensResponse`
