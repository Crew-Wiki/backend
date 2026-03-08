# CrewWiki Backend Style Guide

**Always write all code review comments in Korean.**

This guide defines the coding conventions for the CrewWiki backend project (Java, Spring Boot, Gradle).
Gemini should flag violations of these rules during code review.

---

## 1. Package Structure

Each domain must follow this layered structure:

```
{domain}/
├── controller/
├── dto/
├── repository/
└── service/
```

Shared utilities and infrastructure belong under:

```
common/
├── config/
├── entity/
├── exception/
├── infrastructure/
├── log/
└── utils/
```

---

## 2. Test Code

### 2.1 Naming

- `@Nested` classes must be named after the **production method** they test, in **UpperCamelCase**.
- Test method names must follow this format:
    - `productionMethod_success_condition`
    - `productionMethod_fail_condition`

**Example:**
```java
@Nested
@DisplayName("Login")
class Login {
    @Test
    @DisplayName("Login succeeds with valid credentials")
    void login_success_byValidCredentials() { }

    @Test
    @DisplayName("Login fails with invalid credentials")
    void login_fail_byInvalidCredentials() { }
}
```

### 2.2 BDD Pattern

Tests must follow the BDD structure. The `given` block may be omitted if unnecessary:

```java
// given (omit if not needed)
// when
// then
```

### 2.3 Fixtures

Use static factory methods for test data setup. Do not use constructors directly in tests.

```java
// Correct
CrewDocument doc = DocumentFixture.createDefaultCrewDocument();
```

### 2.4 Test Strategy

- Controller tests: use `RestAssured` when authentication/authorization is required.
- Service tests: use `@SpringBootTest` with `WebEnvironment.MOCK` or `WebEnvironment.NONE` (do not start a Tomcat server).
- Use `assertSoftly` when asserting multiple conditions.
- Prefer **state verification** over behavior verification.
- Service tests are **mandatory**. Controller tests are optional (add when needed).

---

## 3. Naming Conventions

- **Methods**: must start with a verb. Do not include the entity name in the method name.
    - Correct: `memberService.findById(Long id)`
    - Incorrect: `memberService.getMember(Long id)`

- **Path variables**: must be specific and descriptive.
    - Correct: `/api/{documentId}`
    - Incorrect: `/api/{id}`

- **DTOs**: must use `Request` / `Response` suffixes.
    - Registration DTOs may use `Register` instead of `Create`.
    - Examples: `DocumentSearchResponse`, `DocumentUpdateRequest`

---

## 4. Object-Oriented Principles

Apply the following rules as strictly as possible:

1. Only one level of indentation per method.
2. No `else` keyword. Use early returns instead.
3. Wrap all primitives and strings in meaningful types.
4. Only one dot per line (avoid method chaining across multiple objects).
5. No abbreviations in names.
6. Keep classes small and focused.
7. Maximum **2 instance variables** per class.
8. Use first-class collections (wrap collections in dedicated classes).
9. Use getters and setters only when strictly necessary.

---

## 5. Exception Handling

All error responses must include an error code:

```json
{
  "code": "DOCUMENT_NOT_FOUND",
  "message": "..."
}
```

Do not throw generic exceptions. Use project-defined exception types with error codes.

---

## 6. Annotation Order

Annotations must follow this order (top to bottom):

1. Logging (`@Slf4j`)
2. Lombok (`@Getter`, `@NoArgsConstructor`, `@Builder`, etc.)
3. Spring meta (`@EntityListeners`, etc.)
4. Spring component (`@Entity`, `@Table`, `@Service`, `@RestController`, etc.) — always last

**Example:**
```java
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "...")
public class SampleEntity { }
```

---

## 7. Domain Exposure

- Domain objects (entities) must **not** be exposed outside the service layer.
- Controllers must only receive and return DTOs.

**Correct:**
```java
public ApiResponse<SuccessBody<DocumentResponse>> getByUuid(...) { }
```

**Incorrect:**
```java
public Document getByUuid(...) { }
```

---

## 8. API and DTO Mapping

- Each API endpoint must have its own dedicated DTO (1:1 mapping).
- If the same resource has both admin and general endpoints, use separate DTOs.

**Example:**
- Admin document search → `AdminDocumentSearchResponse`
- General document search → `DocumentSearchResponse`

---

## 9. Code Style

- Add one blank line before each class declaration.
- If a method has 2 or more parameters, place each parameter on a new line.
- No restriction on method length; prioritize readability with proper line breaks.

---

## 10. Validation Placement

- **Behavior validation** (e.g., authorization, business rules): in the service layer.
- **State validation** (e.g., field constraints, invariants): in the domain layer.
- Ambiguous cases should be discussed and agreed upon by the team.

---

## 11. Import Rules

- Wildcard imports are **not allowed** (`import com.example.*`).
- Follow IntelliJ IDEA default import ordering.
- Static imports are allowed.

---

## 12. Miscellaneous

- Use `final` for fields and constants.
- Prefer static factory methods over constructors where appropriate.
- Do not use Value Objects (VO) unless explicitly introduced by the team.
- Constants must use `UPPER_SNAKE_CASE`.
- Member ordering within a class: `public` → `protected` → `private`. Getters go at the bottom.

---

## 13. DTO Policy

- DTOs must use `record` by default.
- Use `class` only when `record` is not compatible (e.g., `@ModelAttribute` binding).

---

## 14. Shared Naming (Project-wide)

The following names are already standardized and must be used consistently:

| Type | Name |
|------|------|
| Paged response wrapper | `PagedResponse` |
| Paging request | `PagingRequest` |
| Token info response | `TokenInfoResponse` |
| Auth tokens response | `AuthTokensResponse` |
