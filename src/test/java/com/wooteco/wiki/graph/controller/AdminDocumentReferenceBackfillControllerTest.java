package com.wooteco.wiki.graph.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminDocumentReferenceBackfillControllerTest {

    private static final String BACKFILL_PATH = "/admin/graph/document-references/backfill";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("문서 참조 backfill API를 호출할 때")
    class Backfill {

        @Test
        @DisplayName("token이 없으면 401을 반환한다.")
        void backfill_fail_byMissingToken() {
            // then
            RestAssured
                    .given().log().all()
                    .when().post(BACKFILL_PATH)
                    .then().log().all()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
