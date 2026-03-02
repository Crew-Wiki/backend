package com.wooteco.wiki.organizationevent.controller;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.notNullValue;

import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationevent.domain.OrganizationEvent;
import com.wooteco.wiki.organizationevent.fixture.OrganizationEventFixture;
import com.wooteco.wiki.organizationevent.repository.OrganizationEventRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrganizationEventControllerTest {

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private OrganizationEventRepository organizationEventRepository;

    private OrganizationDocument doc;

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        doc = organizationDocumentRepository.save(OrganizationDocumentFixture.createDefault());
    }

    @Nested
    @DisplayName("조직 이벤트 생성 시")
    class Post {

        @Test
        @DisplayName("유효한 값이면 저장된다.")
        void post_success_byValidRequest() {
            Map<String, Object> body = Map.of(
                    "title", "분기 워크숍",
                    "contents", "OKR 점검",
                    "writer", "밍트",
                    "occurredAt", LocalDate.now().toString(),
                    "organizationDocumentUuid", doc.getUuid().toString()
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/organization-events")
                    .then().log().all()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("data.organizationEventUuid", allOf(notNullValue(), matchesRegex("^[0-9a-fA-F-]{36}$")));
        }

        @Test
        @DisplayName("검증 실패시 400 예외가 발생한다.")
        void post_fail_byValidationError() {
            Map<String, Object> body = Map.of(
                    "title", "   ",
                    "contents", "메모",
                    "writer", "밍트",
                    "occurredAt", LocalDate.now().toString(),
                    "organizationDocumentUuid", doc.getUuid().toString()
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/organization-events")
                    .then().log().all()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("존재하지 않은 조직 문서 uuid일 경우 404 예외가 발생한다.")
        void post_fail_byOrganizationDocumentNotFound() {
            Map<String, Object> body = Map.of(
                    "title", "회의",
                    "contents", "아젠다",
                    "writer", "밍트",
                    "occurredAt", LocalDate.now().toString(),
                    "organizationDocumentUuid", UUID.randomUUID().toString()
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/organization-events")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    @DisplayName("조직 문서를 수정 시")
    class Put {

        @Test
        @DisplayName("전달된 값으로 갱신된다.")
        void put_success_byUpdatedValues() {
            OrganizationEvent event = OrganizationEventFixture.createDefault(doc);
            organizationEventRepository.save(event);
            UUID eventUuid = event.getUuid();

            Map<String, Object> body = Map.of(
                    "title", "분기 워크숍(보강)",
                    "contents", "OKR + 액션아이템",
                    "writer", "밍트",
                    "occurredAt", LocalDate.now().plusDays(1).toString()
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .put("/organization-events/{uuid}", eventUuid)
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.organizationEventUuid", equalTo(eventUuid.toString()))
                    .body("data.title", equalTo("분기 워크숍(보강)"))
                    .body("data.contents", equalTo("OKR + 액션아이템"));
        }

        @Test
        @DisplayName("이벤트가 존재하지 않을 경우 404 예외가 발생한다.")
        void put_fail_byEventNotFound() {
            Map<String, Object> body = Map.of(
                    "title", "x",
                    "contents", "y",
                    "writer", "z",
                    "occurredAt", LocalDate.now().toString()
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .put("/organization-events/{uuid}", UUID.randomUUID())
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        @DisplayName("검증이 실패할 경우 400 예외가 발생한다.")
        void put_fail_byValidationError() {
            OrganizationEvent event = OrganizationEventFixture.createDefault(doc);
            organizationEventRepository.save(event);
            UUID eventUuid = event.getUuid();

            Map<String, Object> body = Map.of(
                    "title", "   ",
                    "contents", "메모",
                    "writer", "밍트",
                    "occurredAt", LocalDate.now().toString()
            );

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .put("/organization-events/{uuid}", eventUuid)
                    .then().log().all()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        }
    }

    @Nested
    @DisplayName("조직 문서 삭제 시")
    class Delete {

        @Test
        @DisplayName("정상적으로 삭제된다.")
        void delete_success_byExistingEvent() {
            OrganizationEvent event = OrganizationEventFixture.createDefault(doc);
            organizationEventRepository.save(event);
            UUID eventUuid = event.getUuid();

            RestAssured.given().log().all()
                    .when()
                    .delete("/organization-events/{uuid}", eventUuid)
                    .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("이벤트 없을 경우 404 예외가 발생한다.")
        void delete_fail_byEventNotFound() {
            RestAssured.given().log().all()
                    .when()
                    .delete("/organization-events/{uuid}", UUID.randomUUID())
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }
}
