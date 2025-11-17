package com.wooteco.wiki.document.service;

import static com.wooteco.wiki.global.exception.ErrorCode.DOCUMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentUuidResponse;
import com.wooteco.wiki.document.fixture.DocumentFixture;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.common.PageRequestDto;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.history.domain.History;
import com.wooteco.wiki.history.fixture.HistoryFixture;
import com.wooteco.wiki.history.repository.HistoryRepository;
import java.time.LocalDateTime;
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
    private DocumentRepository documentRepository;
    @Autowired
    private HistoryRepository historyRepository;

    @DisplayName("문서 조회 기능")
    @Nested
    class Find {

        @DisplayName("문서 조회시, 해당 문서의 마지막 로그 번호를 가져온다.")
        @Test
        void getDocumentLatestVersion_success_byExistsDocument() {
            // given
            CrewDocument crewDocument = DocumentFixture.createDefaultCrewDocument();
            CrewDocument savedCrewDocument = documentRepository.save(crewDocument);

            History history = HistoryFixture.create("test", "test", "tesst", 150, LocalDateTime.of(2025, 7, 15, 10, 0, 0),
                    savedCrewDocument, 20L);
            historyRepository.save(history);

            // when
            DocumentUpdateRequest documentUpdateRequest = new DocumentUpdateRequest("test", "test", "test", 150,
                    savedCrewDocument.getUuid());

            documentService.put(savedCrewDocument.getUuid(), documentUpdateRequest);
            DocumentResponse documentResponse = documentService.getByUuid(savedCrewDocument.getUuid());

            // then
            assertThat(documentResponse.getLatestVersion()).isEqualTo(21L);
        }
    }

    @Nested
    @DisplayName("문서 제목으로 조회하면 UUID를 반환하는 기능")
    class getUuidByTitle {

        @DisplayName("존재하는 문서 제목으로 조회할 경우 UUID를 반환한다")
        @Test
        void getUuidByTitle_success_byExistsDocumentTitle() {
            // given
            DocumentResponse documentResponse = documentService.postCrewDocument(
                    DocumentFixture.createDocumentCreateRequestDefault());

            // when
            DocumentUuidResponse documentUuidResponse = documentService.getUuidByTitle(documentResponse.getTitle());

            // then
            assertThat(documentUuidResponse.uuid()).isEqualTo(documentResponse.getDocumentUUID());
        }

        @DisplayName("존재하지 않는 문서 제목으로 조회할 경우 예외를 반환한다 : WikiException.DOCUMENT_NOT_FOUND")
        @Test
        void getUuidByTitle_success_byNonExistsDocumentTitle() {

            // when & then
            WikiException ex = assertThrows(WikiException.class,
                    () -> documentService.getUuidByTitle("nonExistsDocumentTitle"));
            assertThat(ex.getErrorCode()).isEqualTo(DOCUMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("문서 전체 조회 기능")
    class findAll {

        @Nested
        @DisplayName("문서 전체 조회 기능 : 데이터의 수")
        class findAll_data {

            PageRequestDto pageRequestDto = new PageRequestDto();

            @DisplayName("저장된 문서가 존재할 때 요청 시 List 형태로 반환한다")
            @Test
            void findAll_success_bySomeData() {
                // given
                List<CrewDocumentCreateRequest> crewDocumentCreateRequests = List.of(
                        DocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title2", "content2", "writer2", 11L,
                                UUID.randomUUID())
                );

                // when
                for (CrewDocumentCreateRequest documentRequestDto : crewDocumentCreateRequests) {
                    documentService.postCrewDocument(documentRequestDto);
                }

                // then
                assertThat(documentService.findAll(pageRequestDto)).hasSize(crewDocumentCreateRequests.size());
            }

            @DisplayName("저장된 문서가 존재하지 않을 때 요청 시 예외 없이 빈 리스트를 반환한다")
            @Test
            void findAll_success_byNoData() {
                // when & then
                assertThat(documentService.findAll(pageRequestDto)).hasSize(0);
            }
        }

        @Nested
        @DisplayName("문서 전체 조회 기능 : Pageable")
        class findAll_pageable {

            List<CrewDocumentCreateRequest> crewDocumentCreateRequests;

            @BeforeEach
            public void beforeEach() {
                crewDocumentCreateRequests = List.of(
                        DocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title2", "content2", "writer2", 11L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title3", "content3", "writer3", 13L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title4", "content4", "writer4", 14L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title5", "content5", "writer5", 15L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title6", "content6", "writer6", 16L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title7", "content7", "writer7", 17L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title8", "content8", "writer8", 18L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title9", "content9", "writer9", 19L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title10", "content10", "writer10", 110L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title11", "content11", "writer11", 11L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title12", "content12", "writer12", 11L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title13", "content13", "writer13", 11L,
                                UUID.randomUUID()),
                        DocumentFixture.createDocumentCreateRequest("title14", "content14", "writer14", 11L,
                                UUID.randomUUID())
                );
            }

            @DisplayName("PageRequestDto의 default 값으로 동작하는 지 확인")
            @Test
            void findAll_success_byPageRequestDtoDefault() {
                // given
                PageRequestDto pageRequestDto = new PageRequestDto();

                for (CrewDocumentCreateRequest documentRequestDto : crewDocumentCreateRequests) {
                    documentService.postCrewDocument(documentRequestDto);
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

            @DisplayName("PageRequestDto의 필드 값을 수정한 값으로 동작하는 지 확인")
            @Test
            void findAll_success_byPageRequestDto() {
                // given
                PageRequestDto pageRequestDto = new PageRequestDto();
                pageRequestDto.setPageNumber(1);
                pageRequestDto.setPageSize(5);
                pageRequestDto.setSort("uuid");
                pageRequestDto.setSortDirection("DESC");

                for (CrewDocumentCreateRequest documentRequestDto : crewDocumentCreateRequests) {
                    documentService.postCrewDocument(documentRequestDto);
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

            @DisplayName("PageRequestDto 필드 중 pageNumber 와 pageSize는 음수가 불가능하도록 확인")
            @Test
            void findAll_throwsException_byNegativeNumber() {
                // given
                PageRequestDto pageRequestDto = new PageRequestDto();
                pageRequestDto.setPageNumber(-1);
                pageRequestDto.setPageSize(5);

                for (CrewDocumentCreateRequest documentRequestDto : crewDocumentCreateRequests) {
                    documentService.postCrewDocument(documentRequestDto);
                }

                // when & then
                WikiException ex = assertThrows(WikiException.class,
                        () -> documentService.findAll(pageRequestDto));
                assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PAGE_BAD_REQUEST);
            }
        }
    }

    @Nested
    @DisplayName("문서 uuid로 삭제 기능")
    class deleteByUuid {

        @DisplayName("존재하는 문서 id일 경우 문서가 로그들과 함께 삭제된다")
        @Test
        void deleteById_success_byExistsId() {
            // given
            DocumentResponse documentResponse = documentService.postCrewDocument(
                    DocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L,
                            UUID.randomUUID()));

            // before then
            assertThat(documentRepository.findAll()).hasSize(1);
            assertThat(historyRepository.findAll()).hasSize(1);

            // when
            documentService.deleteByUuid(documentResponse.getDocumentUUID());

            // after then
            assertThat(documentRepository.findAll()).hasSize(0);
            assertThat(historyRepository.findAll()).hasSize(0);
        }

        @DisplayName("존재하지 않는 문서의 id일 경우 예외가 발생한다 : WikiException.DOCUMENT_NOT_FOUND")
        @Test
        void deleteById_throwsException_byNonExistsId() {
            // when & then
            WikiException ex = assertThrows(WikiException.class,
                    () -> documentService.deleteByUuid(UUID.randomUUID()));
            assertThat(ex.getErrorCode()).isEqualTo(DOCUMENT_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("flushViews 호출 시 uuid별로 조회수가 증가된다")
    void flushViews_success() {
        // given
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        CrewDocument doc1 = documentRepository.save(
                DocumentFixture.createCrewDocument("title1", "content1", "writer1", 10L, uuid1));
        CrewDocument doc2 = documentRepository.save(
                DocumentFixture.createCrewDocument("title2", "content2", "writer2", 10L, uuid2));

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
