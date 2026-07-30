package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeType;
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
        @DisplayName("선택한 조직 노드와 현재 기수에서 조직에 연결된 크루 간선을 추가한다.")
        void findByGeneration_success_bySelectedOrganization() {
            // given
            UUID firstCrewUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID secondCrewUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
            UUID organizationDocumentUuid = UUID.fromString("33333333-3333-3333-3333-333333333333");
            OrganizationDocument generation = saveOrganizationDocument("8기");
            OrganizationDocument backend = saveOrganizationDocument(
                    "백엔드",
                    organizationDocumentUuid
            );
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
            saveLink(firstCrew, generation);
            saveLink(firstCrew, backend);
            saveLink(secondCrew, generation);

            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration(
                    "8기",
                    organizationDocumentUuid
            );

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.nodes())
                        .extracting(GraphNodeResponse::documentUuid)
                        .containsExactly(
                                firstCrewUuid,
                                secondCrewUuid,
                                organizationDocumentUuid
                        );
                softly.assertThat(response.nodes())
                        .extracting(GraphNodeResponse::type)
                        .containsExactly(
                                GraphNodeType.CREW,
                                GraphNodeType.CREW,
                                GraphNodeType.ORGANIZATION
                        );
                softly.assertThat(response.edges())
                        .containsExactly(
                                new GraphEdgeResponse(
                                        firstCrewUuid,
                                        secondCrewUuid,
                                        GraphEdgeType.REFERENCE
                                ),
                                new GraphEdgeResponse(
                                        organizationDocumentUuid,
                                        firstCrewUuid,
                                        GraphEdgeType.ORGANIZATION_LINK
                                )
                        );
            });
        }

        @Test
        @DisplayName("선택한 조직 문서가 없으면 조회 예외가 발생한다.")
        void findByGeneration_fail_byMissingOrganizationDocument() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            CrewDocument crewDocument = saveCrewDocument("가람(8기)");
            saveLink(crewDocument, generation);

            // when & then
            assertThatThrownBy(() -> crewGraphQueryService.findByGeneration(
                    "8기",
                    UUID.randomUUID()
            ))
                    .isInstanceOf(WikiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("기수에 속한 크루가 없어도 선택한 조직 문서가 없으면 조회 예외가 발생한다.")
        void findByGeneration_fail_byMissingOrganizationDocumentWithoutGenerationCrew() {
            // when & then
            assertThatThrownBy(() -> crewGraphQueryService.findByGeneration(
                    "8기",
                    UUID.randomUUID()
            ))
                    .isInstanceOf(WikiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("기수 조직을 소속 조직으로 선택하면 검증 예외가 발생한다.")
        void findByGeneration_fail_byGenerationOrganizationSelected() {
            // given
            OrganizationDocument generation = saveOrganizationDocument("8기");
            CrewDocument crewDocument = saveCrewDocument("가람(8기)");
            saveLink(crewDocument, generation);

            // when & then
            assertThatThrownBy(() -> crewGraphQueryService.findByGeneration(
                    "8기",
                    generation.getUuid()
            ))
                    .isInstanceOf(WikiException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("같은 기수 크루 문서의 참조를 중복 없는 무방향 간선으로 반환한다.")
        void findByGeneration_success_byCrewDocumentReferences() {
            // given
            UUID firstCrewUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID secondCrewUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
            OrganizationDocument generation = saveOrganizationDocument("8기");
            CrewDocument firstCrew = saveCrewDocument(
                    "가람(8기)",
                    """
                            [나래](https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222)
                            https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222
                            """,
                    firstCrewUuid
            );
            CrewDocument secondCrew = saveCrewDocument(
                    "나래(8기)",
                    "https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111",
                    secondCrewUuid
            );
            saveLink(firstCrew, generation);
            saveLink(secondCrew, generation);

            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration("8기");

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.nodes()).hasSize(2);
                softly.assertThat(response.edges())
                        .containsExactly(new GraphEdgeResponse(
                                firstCrewUuid,
                                secondCrewUuid,
                                GraphEdgeType.REFERENCE
                        ));
            });
        }

        @Test
        @DisplayName("현재 기수의 다른 크루 문서를 가리키지 않는 참조는 간선에서 제외한다.")
        void findByGeneration_success_byInvalidReferenceTargets() {
            // given
            UUID sourceCrewUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID otherGenerationCrewUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");
            UUID missingDocumentUuid = UUID.fromString("33333333-3333-3333-3333-333333333333");
            UUID organizationDocumentUuid = UUID.fromString("44444444-4444-4444-4444-444444444444");
            OrganizationDocument eighthGeneration = saveOrganizationDocument("8기");
            OrganizationDocument seventhGeneration = saveOrganizationDocument(
                    "7기",
                    organizationDocumentUuid
            );
            CrewDocument sourceCrew = saveCrewDocument(
                    "가람(8기)",
                    """
                            https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111
                            https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222
                            https://crew-wiki.site/wiki/33333333-3333-3333-3333-333333333333
                            https://crew-wiki.site/wiki/44444444-4444-4444-4444-444444444444
                            """,
                    sourceCrewUuid
            );
            CrewDocument otherGenerationCrew = saveCrewDocument(
                    "나래(7기)",
                    "contents",
                    otherGenerationCrewUuid
            );
            saveLink(sourceCrew, eighthGeneration);
            saveLink(otherGenerationCrew, seventhGeneration);

            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration("8기");

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.nodes())
                        .extracting(GraphNodeResponse::documentUuid)
                        .containsExactly(sourceCrewUuid);
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
