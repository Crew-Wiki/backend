package com.wooteco.wiki.organizationdocument.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentCreateRequest;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentUpdateRequest;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentAndEventResponse;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationevent.domain.OrganizationEvent;
import com.wooteco.wiki.organizationevent.fixture.OrganizationEventFixture;
import com.wooteco.wiki.organizationevent.repository.OrganizationEventRepository;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
            assertSoftly(softly -> {
                softly.assertThat(foundOrganizationDocument.getTitle()).isEqualTo(updateTitle);
                softly.assertThat(foundOrganizationDocument.getContents()).isEqualTo(updateContents);
                softly.assertThat(foundOrganizationDocument.getWriter()).isEqualTo(updateWriter);
                softly.assertThat(foundOrganizationDocument.getDocumentBytes()).isEqualTo(updateDocumentBytes);
            });
        }
    }

    @DisplayName("조직 문서를 조회할 때")
    @Nested
    class Find {

        @DisplayName("올바른 값으로 조회된다.")
        @Test
        void find_success_byValidData() {
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
        void create_success_byValidData() {
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
