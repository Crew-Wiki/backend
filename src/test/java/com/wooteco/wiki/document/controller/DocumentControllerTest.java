package com.wooteco.wiki.document.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import io.restassured.RestAssured;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class DocumentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("전체 문서 목록 조회 API를 호출할 때")
    class FindAll {

        @Test
        @DisplayName("페이지 정보와 문서 목록을 응답한다.")
        void findAll_success_byExistingDocument() {
            // given
            UUID documentUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
            CrewDocument document = crewDocumentRepository.save(CrewDocumentFixture.createCrewDocument(
                    "title",
                    "contents",
                    "writer",
                    10L,
                    documentUuid
            ));

            // when & then
            RestAssured.given().log().all()
                    .when()
                    .get("/document")
                    .then().log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("code", equalTo("SUCCESS"))
                    .body("data.page", equalTo(0))
                    .body("data.totalPage", equalTo(1))
                    .body("data.data", hasSize(1))
                    .body("data.data[0].id", equalTo(document.getId().intValue()))
                    .body("data.data[0].title", equalTo("title"))
                    .body("data.data[0].contents", equalTo("contents"))
                    .body("data.data[0].writer", equalTo("writer"))
                    .body("data.data[0].documentBytes", equalTo(10))
                    .body("data.data[0].generateTime", notNullValue())
                    .body("data.data[0].uuid", equalTo(documentUuid.toString()))
                    .body("data.data[0].viewCount", equalTo(0))
                    .body("data.data[0].documentType", equalTo("CREW"));
        }
    }
}
