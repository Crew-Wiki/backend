# CrewWiki Backend Style Guide

This file provides instructions for AI coding agents (Claude Code, Codex, etc.) working on the CrewWiki backend project.

**Always respond and write comments in Korean.**

---

## Project Overview

- **Language**: Java
- **Framework**: Spring Boot
- **Build tool**: Gradle
- **Test**: JUnit 5, RestAssured, AssertJ

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

Shared code belongs under:

```
common/
├── config/
├── entity/
├── exception/
├── infrastructure/
├── log/
└── utils/
```

Do not place domain-specific logic inside `common/`.

---

## 2. Test Code

### 2.1 Naming

- `@Nested` classes must be named after the **production method** they test, in **UpperCamelCase**.
- Test method names must follow this format:
  - `productionMethod_success_condition`
  - `productionMethod_fail_condition`

```java
@Nested
class Login {
    @Test
    void login_success_byValidCredentials() { }

    @Test
    void login_fail_byInvalidCredentials() { }
}
```

### 2.2 BDD Pattern

```java
// given (omit if not needed)
// when
// then
```

### 2.3 Fixtures

Use static factory methods. Do not instantiate test objects with constructors directly.

```java
CrewDocument doc = DocumentFixture.createDefaultCrewDocument();
```

### 2.4 Test Strategy

- Controller tests: use `RestAssured` when authentication/authorization is required.
- Service tests: use `@SpringBootTest(webEnvironment = MOCK or NONE)`. Do not start Tomcat.
- Use `assertSoftly` for multiple assertions.
- Prefer **state verification** over behavior (mock) verification.
- Service tests are **mandatory**. Controller tests are optional.

---

## 3. Naming Conventions

- **Methods**: start with a verb. Do not include the entity name.
  - Correct: `findById(Long id)`
  - Incorrect: `getMember(Long id)`

- **Path variables**: be specific.
  - Correct: `/api/{documentId}`
  - Incorrect: `/api/{id}`

- **DTOs**: use `Request` / `Response` suffixes. `Register` / `Update` are also acceptable for mutations.
  - Examples: `DocumentSearchResponse`, `DocumentUpdateRequest`

---

## 4. Object-Oriented Principles

Follow these rules strictly when writing or modifying code:

1. Only one level of indentation per method.
2. No `else`. Use early returns.
3. Wrap primitives and strings in meaningful types.
4. One dot per line — avoid chaining across objects.
5. No abbreviations.
6. Keep classes small and focused.
7. Aim to minimize instance variables per class. JPA entities and DTOs may exceed this when necessary (e.g., audit fields, associations).
8. Wrap collections in first-class collection classes.
9. Avoid getters and setters unless strictly needed.

---

## 5. Exception Handling

- Do not throw generic exceptions (`RuntimeException`, `Exception`).
- Use project-defined exception types that include an error code.
- All error responses must include a `code` field.

```json
{
  "code": "DOCUMENT_NOT_FOUND",
  "message": "..."
}
```

---

## 6. Annotation Order

When adding annotations, always use this order (top to bottom):

1. Logging (`@Slf4j`)
2. Lombok (`@Getter`, `@NoArgsConstructor`, `@Builder`, etc.)
3. Spring meta (`@EntityListeners`, etc.)
4. Spring component (`@Entity`, `@Table`, `@Service`, `@RestController`, etc.)

```java
@Slf4j
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "...")
public class SampleEntity { }
```

---

## 7. Domain Exposure

- Entities must **not** be returned from controllers or exposed outside the service layer.
- Use DTOs at all boundaries.

```java
// Correct
public ApiResponse<SuccessBody<DocumentResponse>> getByUuid(...) { }

// Incorrect
public Document getByUuid(...) { }
```

---

## 8. API and DTO Mapping

- Each API endpoint must map to its own DTO (1:1).
- Separate DTOs for admin and general endpoints.

```
Admin document search  →  AdminDocumentSearchResponse
General document search  →  DocumentSearchResponse
```

---

## 9. Code Style

- Add one blank line before each class declaration.
- If a method has 2 or more parameters, place each on a new line.
- No restriction on method length; prioritize readability.

---

## 10. Validation Placement

- **Behavior validation** (business rules, authorization): service layer.
- **State validation** (field constraints, invariants): domain layer.

---

## 11. Import Rules

- No wildcard imports (`import com.example.*`).
- Follow IntelliJ IDEA default ordering.
- Static imports are allowed.

---

## 12. Miscellaneous

- Use `final` for fields and constants.
- Prefer static factory methods over constructors.
- Constants: `UPPER_SNAKE_CASE`.
- Member ordering: `public` → `protected` → `private`. Getters go at the bottom.
- Do not introduce Value Objects (VO) without team agreement.

---

## 13. DTO Policy

- Use `record` for DTOs by default.
- Use `class` only when `record` is incompatible (e.g., `@ModelAttribute` binding).

---

## 14. Shared Naming (Do Not Rename)

These names are standardized across the project. Do not rename or create alternatives:

| Type | Name |
|------|------|
| Paged response wrapper | `PagedResponse` |
| Paging request | `PagingRequest` |
| Token info response | `TokenInfoResponse` |
| Auth tokens response | `AuthTokensResponse` |

---

## 15. Agent Behavior Guidelines

- Before making changes, read the relevant domain's existing code to understand patterns in use.
- Do not introduce new libraries or dependencies without explicit instruction.
- When creating a new class, always check if a similar one already exists in `common/`.
- When writing tests, always add a corresponding fixture if one does not exist.
- Do not modify `build.gradle` unless explicitly asked.
- If uncertain about a design decision, leave a `// TODO:` comment explaining the ambiguity rather than guessing.
