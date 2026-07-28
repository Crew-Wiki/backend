package com.wooteco.wiki.document.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.DocumentOrganizationLinkFixture;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import io.restassured.RestAssured;
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
class CrewDocumentQueryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("기수별 크루 조회 API를 호출할 때")
    class FindAllByGeneration {

        @Test
        @DisplayName("성공 응답에 크루 목록을 담아 반환한다.")
        void findAllByGeneration_success_byValidGeneration() {
            // given
            CrewDocument crewDocument = saveCrewDocument("가람 (8기)");
            OrganizationDocument generation = saveOrganizationDocument("8기");
            OrganizationDocument field = saveOrganizationDocument("백엔드");
            saveLink(crewDocument, generation);
            saveLink(crewDocument, field);

            // when & then
            RestAssured.given().log().all()
                    .queryParam("generation", "8기")
                    .when()
                    .get("/document/crews")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("data", hasSize(1))
                    .body("data[0].name", equalTo("가람"))
                    .body("data[0].documentUuid", equalTo(crewDocument.getUuid().toString()))
                    .body("data[0].field", equalTo("BACKEND"));
        }

        @Test
        @DisplayName("기수를 누락하면 검증 실패를 반환한다.")
        void findAllByGeneration_fail_byMissingGeneration() {
            // when & then
            assertValidationError("/document/crews");
        }

        @Test
        @DisplayName("기수가 빈 문자열이면 검증 실패를 반환한다.")
        void findAllByGeneration_fail_byEmptyGeneration() {
            // when & then
            assertValidationError("/document/crews?generation=");
        }

        @Test
        @DisplayName("입력한 조직 제목과 정확히 일치하는 기수가 없으면 빈 목록을 반환한다.")
        void findAllByGeneration_success_byNoExactGenerationTitle() {
            // given
            CrewDocument crewDocument = saveCrewDocument("가람 (8기)");
            OrganizationDocument generation = saveOrganizationDocument("8기");
            saveLink(crewDocument, generation);

            // when & then
            RestAssured.given().log().all()
                    .queryParam("generation", "8")
                    .when()
                    .get("/document/crews")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("data", hasSize(0));
        }
    }

    private void assertValidationError(String path) {
        RestAssured.given().log().all()
                .when()
                .get(path)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo("VALIDATION_ERROR"));
    }

    private CrewDocument saveCrewDocument(String title) {
        CrewDocument crewDocument = CrewDocumentFixture.createCrewDocument(
                title,
                "contents",
                "writer",
                10L,
                UUID.randomUUID()
        );
        return crewDocumentRepository.save(crewDocument);
    }

    private OrganizationDocument saveOrganizationDocument(String title) {
        OrganizationDocument organizationDocument = OrganizationDocumentFixture.create(
                title,
                "contents",
                "writer",
                10L,
                UUID.randomUUID()
        );
        return organizationDocumentRepository.save(organizationDocument);
    }

    private void saveLink(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    ) {
        DocumentOrganizationLink link = DocumentOrganizationLinkFixture.create(
                crewDocument,
                organizationDocument
        );
        documentOrganizationLinkRepository.save(link);
    }
}
