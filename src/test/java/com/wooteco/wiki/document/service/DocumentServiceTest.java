package com.wooteco.wiki.document.service;

import static com.wooteco.wiki.global.exception.ErrorCode.DOCUMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wooteco.wiki.admin.service.CrewDocumentService;
import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUuidResponse;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.common.PagingRequest;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.history.repository.HistoryRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private CrewDocumentService crewDocumentService;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private HistoryRepository historyRepository;



    @Nested
    @DisplayName("문서 제목으로 조회하면 UUID를 반환하는 기능")
    class GetUuidByTitle {

        @DisplayName("존재하는 문서 제목으로 조회할 경우 UUID를 반환한다")
        @Test
        void getUuidByTitle_success_byExistsDocumentTitle() {
            // given
            DocumentResponse documentResponse = crewDocumentService.create(
                    CrewDocumentFixture.createDocumentCreateRequestDefault());

            // when
            DocumentUuidResponse documentUuidResponse = documentService.getUuidByTitle(documentResponse.title());

            // then
            assertThat(documentUuidResponse.uuid()).isEqualTo(documentResponse.documentUUID());
        }

        @DisplayName("존재하지 않는 문서 제목으로 조회할 경우 예외를 반환한다 : WikiException.DOCUMENT_NOT_FOUND")
        @Test
        void getUuidByTitle_fail_byNonExistsDocumentTitle() {

            // when & then
            WikiException ex = assertThrows(WikiException.class,
                    () -> documentService.getUuidByTitle("nonExistsDocumentTitle"));
            assertThat(ex.getErrorCode()).isEqualTo(DOCUMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("문서 전체 조회 기능")
    class FindAll {

        List<CrewDocumentCreateRequest> crewDocumentCreateRequests;

        @BeforeEach
        public void beforeEach() {
            crewDocumentCreateRequests = List.of(
                    CrewDocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title2", "content2", "writer2", 11L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title3", "content3", "writer3", 13L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title4", "content4", "writer4", 14L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title5", "content5", "writer5", 15L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title6", "content6", "writer6", 16L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title7", "content7", "writer7", 17L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title8", "content8", "writer8", 18L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title9", "content9", "writer9", 19L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title10", "content10", "writer10", 110L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title11", "content11", "writer11", 11L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title12", "content12", "writer12", 11L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title13", "content13", "writer13", 11L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title14", "content14", "writer14", 11L,
                            UUID.randomUUID())
            );
        }

        @DisplayName("저장된 문서가 존재할 때 요청 시 List 형태로 반환한다")
        @Test
        void findAll_success_bySomeData() {
            // given
            List<CrewDocumentCreateRequest> requestDtos = List.of(
                    CrewDocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L,
                            UUID.randomUUID()),
                    CrewDocumentFixture.createDocumentCreateRequest("title2", "content2", "writer2", 11L,
                            UUID.randomUUID())
            );

            PagingRequest pageRequestDto = new PagingRequest();

            // when
            for (CrewDocumentCreateRequest documentRequestDto : requestDtos) {
                crewDocumentService.create(documentRequestDto);
            }

            // then
            assertThat(documentService.findAll(pageRequestDto)).hasSize(requestDtos.size());
        }

        @DisplayName("저장된 문서가 존재하지 않을 때 요청 시 예외 없이 빈 리스트를 반환한다")
        @Test
        void findAll_success_byNoData() {
            // given
            PagingRequest pageRequestDto = new PagingRequest();

            // when & then
            assertThat(documentService.findAll(pageRequestDto)).hasSize(0);
        }

        @DisplayName("PagingRequest의 default 값으로 동작하는 지 확인")
        @Test
        void findAll_success_byPagingRequestDefault() {
            // given
            PagingRequest pageRequestDto = new PagingRequest();

            for (CrewDocumentCreateRequest documentRequestDto : crewDocumentCreateRequests) {
                crewDocumentService.create(documentRequestDto);
            }

            // when
            Page<@NotNull Document> documentPages = documentService.findAll(pageRequestDto);

            // then
            SoftAssertions softAssertions = new SoftAssertions();
            softAssertions.assertThat(documentPages.getTotalElements()).isEqualTo(crewDocumentCreateRequests.size());
            softAssertions.assertThat(documentPages.getNumber()).isEqualTo(0);
            softAssertions.assertThat(documentPages.getTotalPages()).isEqualTo(2);
            softAssertions.assertAll();
        }

        @DisplayName("PagingRequest의 필드 값을 수정한 값으로 동작하는 지 확인")
        @Test
        void findAll_success_byPagingRequest() {
            // given
            PagingRequest pageRequestDto = new PagingRequest();
            pageRequestDto.setPageNumber(1);
            pageRequestDto.setPageSize(5);
            pageRequestDto.setSort("uuid");
            pageRequestDto.setSortDirection("DESC");

            for (CrewDocumentCreateRequest documentRequestDto : crewDocumentCreateRequests) {
                crewDocumentService.create(documentRequestDto);
            }

            // when
            Page<@NotNull Document> documentPages = documentService.findAll(pageRequestDto);

            // then
            SoftAssertions softAssertions = new SoftAssertions();
            softAssertions.assertThat(documentPages.getTotalElements()).isEqualTo(crewDocumentCreateRequests.size());
            softAssertions.assertThat(documentPages.getNumber()).isEqualTo(1);
            softAssertions.assertThat(documentPages.getTotalPages()).isEqualTo(3);
            softAssertions.assertAll();
        }

        @DisplayName("PagingRequest 필드 중 pageNumber 와 pageSize는 음수가 불가능하도록 확인")
        @Test
        void findAll_fail_byNegativePageNumber() {
            // given
            PagingRequest pageRequestDto = new PagingRequest();
            pageRequestDto.setPageNumber(-1);
            pageRequestDto.setPageSize(5);

            for (CrewDocumentCreateRequest documentRequestDto : crewDocumentCreateRequests) {
                crewDocumentService.create(documentRequestDto);
            }

            // when & then
            WikiException ex = assertThrows(WikiException.class,
                    () -> documentService.findAll(pageRequestDto));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAGE_BAD_REQUEST);
        }
    }

    @Test
    @DisplayName("flushViews 호출 시 uuid별로 조회수가 증가된다")
    void flushViews_success_byAccumulatedViewCount() {
        // given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        CrewDocument doc1 = documentRepository.save(
                CrewDocumentFixture.createCrewDocument("title1", "content1", "writer1", 10L, uuid1));
        CrewDocument doc2 = documentRepository.save(
                CrewDocumentFixture.createCrewDocument("title2", "content2", "writer2", 10L, uuid2));

        Map<UUID, Integer> viewMap = Map.of(
                uuid1, 5,
                uuid2, 10
        );

        // when
        documentService.flushViews(viewMap);

        // then
        Document updated1 = documentRepository.findById(doc1.getId()).get();
        Document updated2 = documentRepository.findById(doc2.getId()).get();

        Assertions.assertThat(updated1.getViewCount()).isEqualTo(5);
        Assertions.assertThat(updated2.getViewCount()).isEqualTo(10);
    }
}
