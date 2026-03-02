package com.wooteco.wiki.organizationdocument.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.DocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.history.domain.History;
import com.wooteco.wiki.history.repository.HistoryRepository;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentCreateRequest;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentUpdateRequest;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentAndEventResponse;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationevent.domain.OrganizationEvent;
import com.wooteco.wiki.organizationevent.fixture.OrganizationEventFixture;
import com.wooteco.wiki.organizationevent.repository.OrganizationEventRepository;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrganizationCrewDocumentServiceTest {

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private OrganizationDocumentService organizationDocumentService;

    @Autowired
    private OrganizationEventRepository organizationEventRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @DisplayName("조직 문서를 수정할 때")
    @Nested
    class Update {

        @DisplayName("전달된 값으로 갱신된다.")
        @Test
        void updateOrganizationDocument_success_byValidData() {
            // given
            String updateTitle = "updateTitle";
            String updateContents = "updateContents";
            String updateWriter = "updateWriter";
            Long updateDocumentBytes = 200L;

            OrganizationDocument organizationDocument = OrganizationDocumentFixture.createDefault();
            organizationDocumentRepository.save(organizationDocument);
            OrganizationDocumentUpdateRequest organizationDocumentUpdateRequest = new OrganizationDocumentUpdateRequest(
                    updateTitle, updateContents, updateWriter, updateDocumentBytes, organizationDocument.getUuid());

            // when
            organizationDocumentService.update(organizationDocumentUpdateRequest);

            OrganizationDocument foundOrganizationDocument = organizationDocumentRepository.findByUuid(
                    organizationDocument.getUuid()).orElseThrow();

            // then
            Page<History> histories = historyRepository.findAllByDocumentId(organizationDocument.getId(), Pageable.ofSize(1));

            assertSoftly(softly -> {
                softly.assertThat(foundOrganizationDocument.getTitle()).isEqualTo(updateTitle);
                softly.assertThat(foundOrganizationDocument.getContents()).isEqualTo(updateContents);
                softly.assertThat(foundOrganizationDocument.getWriter()).isEqualTo(updateWriter);
                softly.assertThat(foundOrganizationDocument.getDocumentBytes()).isEqualTo(updateDocumentBytes);
                softly.assertThat(histories.hasContent()).isTrue();

                History first = histories.getContent().get(0);
                softly.assertThat(first.getTitle()).isEqualTo(updateTitle);
                softly.assertThat(first.getContents()).isEqualTo(updateContents);
                softly.assertThat(first.getWriter()).isEqualTo(updateWriter);
                softly.assertThat(first.getDocumentBytes()).isEqualTo(updateDocumentBytes);
            });
        }
    }

    @DisplayName("조직 문서를 조회할 때")
    @Nested
    class FindByUuid {

        @DisplayName("올바른 값으로 조회된다.")
        @Test
        void findByUuid_success_byValidData() {
            // given
            UUID uuid = UUID.randomUUID();
            OrganizationDocument organizationDocument = OrganizationDocumentFixture
                    .create("title", "contents", "writer", 15L, uuid);
            organizationDocumentRepository.save(organizationDocument);
            OrganizationEvent organizationEvent = OrganizationEventFixture.createDefault(organizationDocument);
            organizationEventRepository.save(organizationEvent);

            // when
            OrganizationDocumentAndEventResponse organizationDocumentAndEventResponse = organizationDocumentService.findByUuid(
                    uuid);

            // then
            assertSoftly(softly -> {
                softly.assertThat(organizationDocumentAndEventResponse.title()).isEqualTo("title");
                softly.assertThat(organizationDocumentAndEventResponse.contents()).isEqualTo("contents");
                softly.assertThat(organizationDocumentAndEventResponse.writer()).isEqualTo("writer");
                softly.assertThat(organizationDocumentAndEventResponse.organizationDocumentUuid()).isEqualTo(uuid);
                softly.assertThat(organizationDocumentAndEventResponse.organizationEventResponses().size())
                        .isEqualTo(1);
                softly.assertThat(organizationDocumentAndEventResponse.organizationEventResponses().get(0).title())
                        .isEqualTo("defaultTitle");
            });
        }
    }

    @DisplayName("조직 문서를 생성할 때")
    @Nested
    class Create {

        @DisplayName("이미 있는 문서 이름이라면 예외가 발생한다.")
        @Test
        void create_fail_byDuplicateTitle() {
            // given
            UUID uuid = UUID.randomUUID();
            OrganizationDocument organizationDocument = OrganizationDocumentFixture
                    .create("title", "contents", "writer", 15L, uuid);
            organizationDocumentRepository.save(organizationDocument);
            OrganizationEvent organizationEvent = OrganizationEventFixture.createDefault(organizationDocument);
            organizationEventRepository.save(organizationEvent);

            // when
            OrganizationDocumentCreateRequest organizationDocumentCreateRequest = new OrganizationDocumentCreateRequest(
                    "title", "contents", "writer", 15L, UUID.randomUUID(), UUID.randomUUID());

            WikiException ex = assertThrows(WikiException.class,
                    () -> organizationDocumentService.create(organizationDocumentCreateRequest));
            Assertions.assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_DUPLICATE);
        }

        @DisplayName("첫 번째 로그가 저장된다.")
        @Test
        void create_success_byFirstHistorySaved() {
            // given
            CrewDocument crewDocument = DocumentFixture.createDefaultCrewDocument();
            CrewDocument savedCrewDocument = crewDocumentRepository.save(crewDocument);

            OrganizationDocumentCreateRequest organizationDocumentCreateRequest = new OrganizationDocumentCreateRequest(
                    "newTitle", "newContents", "newWriter", 99L, savedCrewDocument.getUuid(), UUID.randomUUID());

            // when
            OrganizationDocumentResponse response = organizationDocumentService.create(organizationDocumentCreateRequest);
            OrganizationDocument savedOrganizationDocument = organizationDocumentRepository.findByUuid(
                    response.organizationDocumentUuid()).orElseThrow();

            // then
            Page<History> histories = historyRepository.findAllByDocumentId(savedOrganizationDocument.getId(),
                    Pageable.ofSize(1));

            assertSoftly(softly -> {
                softly.assertThat(histories.hasContent()).isTrue();
                History first = histories.getContent().get(0);
                softly.assertThat(first.getVersion()).isEqualTo(1L);
                softly.assertThat(first.getTitle()).isEqualTo("newTitle");
                softly.assertThat(first.getContents()).isEqualTo("newContents");
                softly.assertThat(first.getWriter()).isEqualTo("newWriter");
                softly.assertThat(first.getDocumentBytes()).isEqualTo(99L);
            });
        }

//        @DisplayName("존재하지 않는 특정 문서의 Uuid로 요청한다면 예외가 발생한다 : DOCUMENT_NOT_FOUND")
//        @Test
//        void addOrganizationDocument_error_byNonExistingDocumentUuid() {
//            // when & then
//            WikiException ex = assertThrows(WikiException.class,
//                    () -> organizationDocumentService.create(UUID.randomUUID(),
//                            documentOrganizationMappingAddRequest));
//            Assertions.assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND);
//        }
    }
}
