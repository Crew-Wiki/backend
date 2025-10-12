package com.wooteco.wiki.document.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.DocumentType;
import com.wooteco.wiki.document.domain.dto.DocumentSearchResponse;
import com.wooteco.wiki.document.fixture.DocumentFixture;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DocumentSearchServiceTest {

    @Autowired
    private DocumentSearchService documentSearchService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @DisplayName("검색어로 시작하는 문서들을 찾아 리스트로 반환한다.")
    @Test
    void search_success() {
        // given
        documentService.postCrewDocument(
                DocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L, UUID.randomUUID()));
        documentService.postCrewDocument(
                DocumentFixture.createDocumentCreateRequest("title2", "content2", "writer2", 11L, UUID.randomUUID()));

        // when
        List<DocumentSearchResponse> result = documentSearchService.search("title");

        // then
        Assertions.assertThat(result).hasSize(2);
    }

    @DisplayName("검색어와 일치하는 문서가 없으면 빈 리스트를 반환한다.")
    @Test
    void search_empty() {
        // given
        String keyword = "존재하지않는문서";

        // when
        List<DocumentSearchResponse> result = documentSearchService.search(keyword);

        // then
        Assertions.assertThat(result).isEmpty();
    }

    @DisplayName("조직 문서라면 타입이 조직문서로 나온다.")
    @Test
    void search_success_has_valid_type() {
        // given
        documentService.postCrewDocument(
                DocumentFixture.createDocumentCreateRequest("title1", "content1", "writer1", 10L, UUID.randomUUID()));
        OrganizationDocument organizationDocument = OrganizationDocumentFixture.create("title2", "content", "writer",
                15L, UUID.randomUUID());
        organizationDocumentRepository.save(organizationDocument);

        // when
        List<DocumentSearchResponse> result = documentSearchService.search("title");

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).getDocumentType()).isEqualTo(DocumentType.CREW);
            softly.assertThat(result.get(1).getDocumentType()).isEqualTo(DocumentType.ORGANIZATION);
        });
    }
} 
