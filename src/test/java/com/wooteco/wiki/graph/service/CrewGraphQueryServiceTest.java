package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.dto.GraphNodeResponse;
import com.wooteco.wiki.graph.dto.GraphNodeType;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.DocumentOrganizationLinkFixture;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CrewGraphQueryServiceTest {

    @Autowired
    private CrewGraphQueryService crewGraphQueryService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @Nested
    @DisplayName("기수별 크루 그래프를 조회할 때")
    class FindByGeneration {

        @Test
        @DisplayName("요청한 기수 조직과 연결된 크루 문서만 노드로 반환한다.")
        void findByGeneration_success_byMatchingGenerationTitle() {
            // given
            OrganizationDocument eighthGeneration = saveOrganizationDocument("8기");
            OrganizationDocument seventhGeneration = saveOrganizationDocument("7기");
            CrewDocument eighthCrew = saveCrewDocument("가람(8기)");
            CrewDocument anotherEighthCrew = saveCrewDocument("나래(8기)");
            CrewDocument seventhCrew = saveCrewDocument("다온(7기)");
            saveLink(eighthCrew, eighthGeneration);
            saveLink(anotherEighthCrew, eighthGeneration);
            saveLink(seventhCrew, seventhGeneration);

            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration("8기");

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.nodes())
                        .extracting(GraphNodeResponse::documentUuid)
                        .containsExactly(eighthCrew.getUuid(), anotherEighthCrew.getUuid());
                softly.assertThat(response.nodes())
                        .extracting(GraphNodeResponse::title)
                        .containsExactly("가람(8기)", "나래(8기)");
                softly.assertThat(response.nodes())
                        .extracting(GraphNodeResponse::type)
                        .containsOnly(GraphNodeType.CREW);
                softly.assertThat(response.edges()).isEmpty();
            });
        }

        @Test
        @DisplayName("요청한 기수 조직이 없으면 빈 그래프를 반환한다.")
        void findByGeneration_success_byMissingGenerationOrganization() {
            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration("8기");

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.nodes()).isEmpty();
                softly.assertThat(response.edges()).isEmpty();
            });
        }

        @Test
        @DisplayName("기수가 공백이면 검증 예외가 발생한다.")
        void findByGeneration_fail_byBlankGeneration() {
            // when & then
            assertThatThrownBy(() -> crewGraphQueryService.findByGeneration(" "))
                    .isInstanceOf(WikiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("기수가 null이면 검증 예외가 발생한다.")
        void findByGeneration_fail_byNullGeneration() {
            // when & then
            assertThatThrownBy(() -> crewGraphQueryService.findByGeneration(null))
                    .isInstanceOf(WikiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VALIDATION_ERROR);
        }
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
