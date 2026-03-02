package com.wooteco.wiki.organizationevent.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationevent.domain.OrganizationEvent;
import com.wooteco.wiki.organizationevent.dto.request.OrganizationEventCreateRequest;
import com.wooteco.wiki.organizationevent.dto.request.OrganizationEventUpdateRequest;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventCreateResponse;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventUpdateResponse;
import com.wooteco.wiki.organizationevent.repository.OrganizationEventRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrganizationEventServiceTest {

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private OrganizationEventRepository organizationEventRepository;

    @Autowired
    private OrganizationEventService organizationEventService;

    @Nested
    @DisplayName("조직 이벤트 생성 시")
    class Post {

        @Test
        @DisplayName("유효한 값이면 저장되고 UUID가 반환된다.")
        void post_success_byValidRequest() {
            // given
            OrganizationDocument orgDoc = OrganizationDocumentFixture.createDefault();
            organizationDocumentRepository.save(orgDoc);

            OrganizationEventCreateRequest req = new OrganizationEventCreateRequest(
                    "분기 워크숍",
                    "OKR 점검",
                    "밍트",
                    LocalDate.now(),
                    orgDoc.getUuid()
            );

            // when
            OrganizationEventCreateResponse res = organizationEventService.post(req);

            // then
            OrganizationEvent saved = organizationEventRepository.findByUuid(res.organizationEventUuid())
                    .orElseThrow();

            assertSoftly(softly -> {
                softly.assertThat(saved.getUuid()).isEqualTo(res.organizationEventUuid());
                softly.assertThat(saved.getTitle()).isEqualTo(req.title());
                softly.assertThat(saved.getContents()).isEqualTo(req.contents());
                softly.assertThat(saved.getWriter()).isEqualTo(req.writer());
                softly.assertThat(saved.getOccurredAt()).isEqualTo(req.occurredAt());
                softly.assertThat(saved.getOrganizationDocument().getId())
                        .isEqualTo(orgDoc.getId());
            });
        }

        @Test
        @DisplayName("존재하지 않는 조직 문서 UUID면 404 예외가 발생한다.")
        void post_fail_byOrganizationDocumentNotFound() {
            // given
            OrganizationEventCreateRequest req = new OrganizationEventCreateRequest(
                    "회의",
                    "아젠다",
                    "밍트",
                    LocalDate.now(),
                    UUID.randomUUID()
            );

            // then
            assertThatThrownBy(() -> organizationEventService.post(req))
                    .isInstanceOf(WikiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("조직 이벤트 수정 시")
    class Put {

        @Test
        @DisplayName("전달된 값으로 갱신된다.")
        void put_success_byUpdatedValues() {
            // given: 선행으로 문서 + 이벤트 하나 생성
            OrganizationDocument orgDoc = OrganizationDocumentFixture.createDefault();
            organizationDocumentRepository.save(orgDoc);

            OrganizationEventCreateRequest createReq = new OrganizationEventCreateRequest(
                    "분기 워크숍",
                    "OKR 점검",
                    "밍트",
                    LocalDate.now(),
                    orgDoc.getUuid()
            );
            UUID eventUuid = organizationEventService.post(createReq).organizationEventUuid();

            OrganizationEventUpdateRequest req = new OrganizationEventUpdateRequest(
                    "분기 워크숍(보강)",
                    "OKR + 액션아이템",
                    "밍트2",
                    LocalDate.now().plusDays(1)
            );

            // when
            OrganizationEventUpdateResponse res = organizationEventService.put(eventUuid, req);

            // then
            OrganizationEvent found = organizationEventRepository.findByUuid(eventUuid).orElseThrow();

            assertSoftly(softly -> {
                softly.assertThat(res.organizationEventUuid()).isEqualTo(eventUuid);
                softly.assertThat(found.getTitle()).isEqualTo(req.title());
                softly.assertThat(found.getContents()).isEqualTo(req.contents());
                softly.assertThat(found.getWriter()).isEqualTo(req.writer());
                softly.assertThat(found.getOccurredAt()).isEqualTo(req.occurredAt());
            });
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 UUID면 404 예외가 발생한다.")
        void put_fail_byEventNotFound() {
            // given
            OrganizationEventUpdateRequest updateReq = new OrganizationEventUpdateRequest(
                    "x", "y", "z", LocalDate.now()
            );

            // then
            assertThatThrownBy(() ->
                    organizationEventService.put(UUID.randomUUID(), updateReq))
                    .isInstanceOf(WikiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_EVENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("조직 이벤트 삭제 시")
    class Delete {

        @Test
        @DisplayName("정상적으로 삭제된다.")
        void delete_success_byExistingEvent() {
            // given
            OrganizationDocument orgDoc = OrganizationDocumentFixture.createDefault();
            organizationDocumentRepository.save(orgDoc);

            OrganizationEventCreateRequest createReq = new OrganizationEventCreateRequest(
                    "분기 워크숍",
                    "OKR 점검",
                    "밍트",
                    LocalDate.now(),
                    orgDoc.getUuid()
            );
            UUID eventUuid = organizationEventService.post(createReq).organizationEventUuid();

            // when
            organizationEventService.delete(eventUuid);

            // then
            assertSoftly(softly -> {
                softly.assertThat(organizationEventRepository.findByUuid(eventUuid)).isEmpty();
            });
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 UUID면 404 예외가 발생한다.")
        void delete_fail_byEventNotFound() {
            // then
            assertThatThrownBy(() -> organizationEventService.delete(UUID.randomUUID()))
                    .isInstanceOf(WikiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORGANIZATION_EVENT_NOT_FOUND);
        }
    }
}
