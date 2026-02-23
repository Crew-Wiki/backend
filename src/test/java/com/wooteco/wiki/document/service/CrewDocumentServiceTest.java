package com.wooteco.wiki.document.service;

import static com.wooteco.wiki.global.exception.ErrorCode.DOCUMENT_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wooteco.wiki.admin.service.CrewDocumentService;
import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest;
import com.wooteco.wiki.document.fixture.DocumentFixture;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.history.domain.History;
import com.wooteco.wiki.history.fixture.HistoryFixture;
import com.wooteco.wiki.history.repository.HistoryRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CrewDocumentServiceTest {

    @Autowired
    private CrewDocumentService crewDocumentService;
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

            History history = HistoryFixture.create("test", "test", "tesst", 150,
                LocalDateTime.of(2025, 7, 15, 10, 0, 0),
                savedCrewDocument, 20L);
            historyRepository.save(history);

            // when
            DocumentUpdateRequest documentUpdateRequest = new DocumentUpdateRequest("test", "test", "test", 150L,
                savedCrewDocument.getUuid());

            crewDocumentService.update(savedCrewDocument.getUuid(), documentUpdateRequest);
            DocumentResponse documentResponse = crewDocumentService.getByUuid(savedCrewDocument.getUuid());

            // then
            assertThat(documentResponse.latestVersion()).isEqualTo(21L);
        }
    }

    @Nested
    @DisplayName("문서 uuid로 삭제 기능")
    class deleteByUuid {

        @DisplayName("존재하는 문서 id일 경우 문서가 로그들과 함께 삭제된다")
        @Test
        void deleteById_success_byExistsId() {
            // given
            DocumentResponse documentResponse = crewDocumentService.create(
                DocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L,
                    UUID.randomUUID()));

            // before then
            assertThat(documentRepository.findAll()).hasSize(1);
            assertThat(historyRepository.findAll()).hasSize(1);

            // when
            crewDocumentService.deleteByUuid(documentResponse.documentUUID());

            // after then
            assertThat(documentRepository.findAll()).hasSize(0);
            assertThat(historyRepository.findAll()).hasSize(0);
        }

        @DisplayName("존재하지 않는 문서의 id일 경우 예외가 발생한다 : WikiException.DOCUMENT_NOT_FOUND")
        @Test
        void deleteById_throwsException_byNonExistsId() {
            // when & then
            WikiException ex = assertThrows(WikiException.class,
                () -> crewDocumentService.deleteByUuid(UUID.randomUUID()));
            assertThat(ex.getErrorCode()).isEqualTo(DOCUMENT_NOT_FOUND);
        }
    }
}
