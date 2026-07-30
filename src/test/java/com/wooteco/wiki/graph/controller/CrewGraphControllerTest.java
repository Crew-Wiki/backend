package com.wooteco.wiki.graph.controller;

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
class CrewGraphControllerTest {

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
    @DisplayName("크루 그래프 조회 API를 호출할 때")
    class FindByGeneration {

        @Test
        @DisplayName("성공 응답에 기수와 일치하는 크루 문서 노드와 참조 간선을 담아 반환한다.")
        void findByGeneration_success_byMatchingGenerationTitle() {
            // given
            UUID firstCrewUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID secondCrewUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
            CrewDocument firstCrew = saveCrewDocument(
                    "가람(8기)",
                    "https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222",
                    firstCrewUuid
            );
            CrewDocument secondCrew = saveCrewDocument(
                    "나래(8기)",
                    "contents",
                    secondCrewUuid
            );
            OrganizationDocument generation = saveOrganizationDocument("8기");
            saveLink(firstCrew, generation);
            saveLink(secondCrew, generation);

            // when & then
            RestAssured.given().log().all()
                    .queryParam("generation", "8기")
                    .when()
                    .get("/graph")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.nodes", hasSize(2))
                    .body("data.nodes[0].documentUuid", equalTo(firstCrewUuid.toString()))
                    .body("data.nodes[0].title", equalTo("가람(8기)"))
                    .body("data.nodes[0].type", equalTo("CREW"))
                    .body("data.nodes[1].documentUuid", equalTo(secondCrewUuid.toString()))
                    .body("data.nodes[1].title", equalTo("나래(8기)"))
                    .body("data.nodes[1].type", equalTo("CREW"))
                    .body("data.edges", hasSize(1))
                    .body("data.edges[0].sourceDocumentUuid", equalTo(firstCrewUuid.toString()))
                    .body("data.edges[0].targetDocumentUuid", equalTo(secondCrewUuid.toString()))
                    .body("data.edges[0].type", equalTo("REFERENCE"));
        }

        @Test
        @DisplayName("선택한 조직 노드와 현재 기수에서 조직에 연결된 크루 간선을 반환한다.")
        void findByGeneration_success_bySelectedOrganization() {
            // given
            UUID firstCrewUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID secondCrewUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
            UUID organizationDocumentUuid = UUID.fromString("33333333-3333-3333-3333-333333333333");
            CrewDocument firstCrew = saveCrewDocument(
                    "가람(8기)",
                    "https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222",
                    firstCrewUuid
            );
            CrewDocument secondCrew = saveCrewDocument(
                    "나래(8기)",
                    "contents",
                    secondCrewUuid
            );
            OrganizationDocument generation = saveOrganizationDocument("8기");
            OrganizationDocument backend = saveOrganizationDocument(
                    "백엔드",
                    organizationDocumentUuid
            );
            saveLink(firstCrew, generation);
            saveLink(firstCrew, backend);
            saveLink(secondCrew, generation);

            // when & then
            RestAssured.given().log().all()
                    .queryParam("generation", "8기")
                    .queryParam("organizationDocumentUuid", organizationDocumentUuid)
                    .when()
                    .get("/graph")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.nodes", hasSize(3))
                    .body("data.nodes[0].documentUuid", equalTo(firstCrewUuid.toString()))
                    .body("data.nodes[0].type", equalTo("CREW"))
                    .body("data.nodes[1].documentUuid", equalTo(secondCrewUuid.toString()))
                    .body("data.nodes[1].type", equalTo("CREW"))
                    .body("data.nodes[2].documentUuid", equalTo(organizationDocumentUuid.toString()))
                    .body("data.nodes[2].title", equalTo("백엔드"))
                    .body("data.nodes[2].type", equalTo("ORGANIZATION"))
                    .body("data.edges", hasSize(2))
                    .body("data.edges[0].sourceDocumentUuid", equalTo(firstCrewUuid.toString()))
                    .body("data.edges[0].targetDocumentUuid", equalTo(secondCrewUuid.toString()))
                    .body("data.edges[0].type", equalTo("REFERENCE"))
                    .body("data.edges[1].sourceDocumentUuid", equalTo(organizationDocumentUuid.toString()))
                    .body("data.edges[1].targetDocumentUuid", equalTo(firstCrewUuid.toString()))
                    .body("data.edges[1].type", equalTo("ORGANIZATION_LINK"));
        }

        @Test
        @DisplayName("선택한 조직 문서가 없으면 조회 실패를 반환한다.")
        void findByGeneration_fail_byMissingOrganizationDocument() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            CrewDocument crewDocument = saveCrewDocument("가람(8기)");
            saveLink(crewDocument, generation);

            // when & then
            RestAssured.given().log().all()
                    .queryParam("generation", "8기")
                    .queryParam("organizationDocumentUuid", UUID.randomUUID())
                    .when()
                    .get("/graph")
                    .then().log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("code", equalTo("ORGANIZATION_DOCUMENT_NOT_FOUND"));
        }

        @Test
        @DisplayName("선택한 조직 문서 UUID 형식이 잘못되면 검증 실패를 반환한다.")
        void findByGeneration_fail_byInvalidOrganizationDocumentUuid() {
            // when & then
            RestAssured.given().log().all()
                    .queryParam("generation", "8기")
                    .queryParam("organizationDocumentUuid", "invalid-uuid")
                    .when()
                    .get("/graph")
                    .then().log().all()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .body("code", equalTo("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("입력한 조직 제목과 정확히 일치하는 기수가 없으면 빈 그래프를 반환한다.")
        void findByGeneration_success_byNoExactGenerationTitle() {
            // given
            CrewDocument crewDocument = saveCrewDocument("가람(8기)");
            OrganizationDocument generation = saveOrganizationDocument("8기");
            saveLink(crewDocument, generation);

            // when & then
            RestAssured.given().log().all()
                    .queryParam("generation", "8")
                    .when()
                    .get("/graph")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("data.nodes", hasSize(0))
                    .body("data.edges", hasSize(0));
        }

        @Test
        @DisplayName("기수를 누락하면 검증 실패를 반환한다.")
        void findByGeneration_fail_byMissingGeneration() {
            // when & then
            assertValidationError("/graph");
        }

        @Test
        @DisplayName("기수가 빈 문자열이면 검증 실패를 반환한다.")
        void findByGeneration_fail_byEmptyGeneration() {
            // when & then
            assertValidationError("/graph?generation=");
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
        return saveCrewDocument(title, "contents", UUID.randomUUID());
    }

    private CrewDocument saveCrewDocument(
            String title,
            String contents,
            UUID uuid
    ) {
        CrewDocument crewDocument = CrewDocumentFixture.createCrewDocument(
                title,
                contents,
                "writer",
                10L,
                uuid
        );
        return crewDocumentRepository.save(crewDocument);
    }

    private OrganizationDocument saveOrganizationDocument(String title) {
        return saveOrganizationDocument(title, UUID.randomUUID());
    }

    private OrganizationDocument saveOrganizationDocument(
            String title,
            UUID uuid
    ) {
        OrganizationDocument organizationDocument = OrganizationDocumentFixture.create(
                title,
                "contents",
                "writer",
                10L,
                uuid
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
