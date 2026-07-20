package com.wooteco.wiki.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.CrewField;
import com.wooteco.wiki.document.dto.GenerationCrewResponse;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.DocumentOrganizationLinkFixture;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CrewDocumentQueryServiceTest {

    @Autowired
    private CrewDocumentQueryService crewDocumentQueryService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @Nested
    @DisplayName("기수별 크루를 조회할 때")
    class FindAllByGeneration {

        @Test
        @DisplayName("요청한 기수에 속한 크루만 반환한다.")
        void findAllByGeneration_success_byMatchingGeneration() {
            // given
            OrganizationDocument eighthGeneration = saveOrganizationDocument("8기");
            OrganizationDocument seventhGeneration = saveOrganizationDocument("7기");
            CrewDocument eighthCrew = saveCrewDocument("가람(8기)");
            CrewDocument seventhCrew = saveCrewDocument("나래(7기)");
            saveLink(eighthCrew, eighthGeneration);
            saveLink(seventhCrew, seventhGeneration);

            // when
            List<GenerationCrewResponse> responses = crewDocumentQueryService.findAllByGeneration(8);

            // then
            assertSoftly(softly -> {
                softly.assertThat(responses).hasSize(1);
                softly.assertThat(responses.get(0).name()).isEqualTo("가람");
                softly.assertThat(responses.get(0).documentUuid()).isEqualTo(eighthCrew.getUuid());
            });
        }

        @Test
        @DisplayName("허용된 소속을 크루 분야로 반환한다.")
        void findAllByGeneration_success_byAllowedFields() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            OrganizationDocument backend = saveOrganizationDocument("백엔드");
            OrganizationDocument frontend = saveOrganizationDocument("프론트엔드");
            OrganizationDocument android = saveOrganizationDocument("안드로이드");
            OrganizationDocument unknown = saveOrganizationDocument("데브옵스");
            CrewDocument backendCrew = saveCrewDocument("가람(8기)");
            CrewDocument frontendCrew = saveCrewDocument("나래(8기)");
            CrewDocument androidCrew = saveCrewDocument("다온(8기)");
            saveLinks(backendCrew, generation, backend, unknown);
            saveLinks(frontendCrew, generation, frontend);
            saveLinks(androidCrew, generation, android);

            // when
            List<GenerationCrewResponse> responses = crewDocumentQueryService.findAllByGeneration(8);

            // then
            assertThat(responses)
                    .extracting(GenerationCrewResponse::field)
                    .containsExactly(CrewField.BACKEND, CrewField.FRONTEND, CrewField.ANDROID);
        }

        @Test
        @DisplayName("분야가 없거나 알 수 없거나 복수이면 null을 반환한다.")
        void findAllByGeneration_success_byIndeterminateFields() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            OrganizationDocument unknown = saveOrganizationDocument("데브옵스");
            OrganizationDocument backend = saveOrganizationDocument("백엔드");
            OrganizationDocument frontend = saveOrganizationDocument("프론트엔드");
            CrewDocument noFieldCrew = saveCrewDocument("가람(8기)");
            CrewDocument unknownFieldCrew = saveCrewDocument("나래(8기)");
            CrewDocument multipleFieldCrew = saveCrewDocument("다온(8기)");
            saveLink(noFieldCrew, generation);
            saveLinks(unknownFieldCrew, generation, unknown);
            saveLinks(multipleFieldCrew, generation, backend, frontend);

            // when
            List<GenerationCrewResponse> responses = crewDocumentQueryService.findAllByGeneration(8);

            // then
            assertThat(responses)
                    .extracting(GenerationCrewResponse::field)
                    .containsExactly(null, null, null);
        }

        @Test
        @DisplayName("제목의 첫 괄호 앞 문자열을 이름으로 반환한다.")
        void findAllByGeneration_success_byCrewTitleFormats() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            CrewDocument spacedCrew = saveCrewDocument("  가람 (8기)");
            CrewDocument unspacedCrew = saveCrewDocument("나래(8기)");
            CrewDocument plainCrew = saveCrewDocument("  라온  ");
            saveLink(spacedCrew, generation);
            saveLink(unspacedCrew, generation);
            saveLink(plainCrew, generation);

            // when
            List<GenerationCrewResponse> responses = crewDocumentQueryService.findAllByGeneration(8);

            // then
            assertThat(responses)
                    .extracting(GenerationCrewResponse::name)
                    .containsExactly("가람", "나래", "라온");
        }

        @Test
        @DisplayName("제목에서 추출한 이름이 비어 있으면 제외한다.")
        void findAllByGeneration_success_byBlankExtractedName() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            CrewDocument blankNameCrew = saveCrewDocument("   (8기)");
            saveLink(blankNameCrew, generation);

            // when
            List<GenerationCrewResponse> responses = crewDocumentQueryService.findAllByGeneration(8);

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("이름 오름차순으로 정렬해 반환한다.")
        void findAllByGeneration_success_byNameAscendingOrder() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            CrewDocument thirdCrew = saveCrewDocument("다온(8기)");
            CrewDocument firstCrew = saveCrewDocument("가람(8기)");
            CrewDocument secondCrew = saveCrewDocument("나래(8기)");
            saveLink(thirdCrew, generation);
            saveLink(firstCrew, generation);
            saveLink(secondCrew, generation);

            // when
            List<GenerationCrewResponse> responses = crewDocumentQueryService.findAllByGeneration(8);

            // then
            assertThat(responses)
                    .extracting(GenerationCrewResponse::name)
                    .containsExactly("가람", "나래", "다온");
        }

        @Test
        @DisplayName("해당 기수의 크루가 없으면 빈 목록을 반환한다.")
        void findAllByGeneration_success_byNoCrew() {
            // when
            List<GenerationCrewResponse> responses = crewDocumentQueryService.findAllByGeneration(8);

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("기수가 0이면 검증 예외가 발생한다.")
        void findAllByGeneration_fail_byZeroGeneration() {
            // when & then
            assertThatThrownBy(() -> crewDocumentQueryService.findAllByGeneration(0))
                    .isInstanceOf(WikiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("기수가 음수면 검증 예외가 발생한다.")
        void findAllByGeneration_fail_byNegativeGeneration() {
            // when & then
            assertThatThrownBy(() -> crewDocumentQueryService.findAllByGeneration(-1))
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

    private void saveLinks(
            CrewDocument crewDocument,
            OrganizationDocument... organizationDocuments
    ) {
        for (OrganizationDocument organizationDocument : organizationDocuments) {
            saveLink(crewDocument, organizationDocument);
        }
    }
}
