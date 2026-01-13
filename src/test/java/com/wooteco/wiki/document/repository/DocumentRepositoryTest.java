package com.wooteco.wiki.document.repository;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.DocumentType;
import com.wooteco.wiki.document.fixture.DocumentFixture;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Nested
    @DisplayName("문서 제목으로 uuid를 조회하는 기능")
    class findUuidByTitle {

        @DisplayName("존재하지 않는 문서 제목으로 조회했을 때 Optional.empty를 반환한다")
        @Test
        void findUuidByTitle_success_byNonExistsDocument() {

            // when
            Optional<UUID> actual = documentRepository.findUuidByTitle("nonExistsDocumentTitle");

            // then
            Assertions.assertThat(actual).isEmpty();
        }

        @DisplayName("존재하는 문서 제목으로 조회했을 때 Optional(UUID)를 반환한다")
        @Test
        void findUuidByTitle_success_byExistsDocument() {
            // given
            CrewDocument savedCrewDocument = documentRepository.save(DocumentFixture.createDefaultCrewDocument());

            // when
            Optional<UUID> actual = documentRepository.findUuidByTitle(savedCrewDocument.getTitle());

            // then
            Assertions.assertThat(actual.get()).isEqualTo(savedCrewDocument.getUuid());
        }
    }

    @Nested
    @DisplayName("문서 전체 조회 기능")
    class findAll {

        @DisplayName("DocumentRepository로 조회 시 CrewDocument와 OrganizationDocument가 모두 조회된다")
        @Test
        void findAll_success_returnsBothCrewAndOrganizationDocuments() {
            // given
            documentRepository.save(
                    DocumentFixture.createCrewDocument("crew문서", "content1", "writer1", 10L, UUID.randomUUID()));
            documentRepository.save(
                    OrganizationDocumentFixture.create("org문서", "content2", "writer2", 15L, UUID.randomUUID()));

            // when
            List<Document> result = documentRepository.findAll();

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result).extracting(Document::type)
                        .containsExactlyInAnyOrder(DocumentType.CREW, DocumentType.ORGANIZATION);
                softly.assertThat(result).extracting(Document::getTitle)
                        .containsExactlyInAnyOrder("crew문서", "org문서");
            });
        }

        @DisplayName("CrewDocument만 저장했을 때는 CrewDocument만 조회된다")
        @Test
        void findAll_success_returnsOnlyCrewDocuments() {
            // given
            documentRepository.save(
                    DocumentFixture.createCrewDocument("crew1", "content1", "writer1", 10L, UUID.randomUUID()));
            documentRepository.save(
                    DocumentFixture.createCrewDocument("crew2", "content2", "writer2", 15L, UUID.randomUUID()));

            // when
            List<Document> result = documentRepository.findAll();

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result).allMatch(doc -> doc.type() == DocumentType.CREW);
            });
        }

        @DisplayName("OrganizationDocument만 저장했을 때는 OrganizationDocument만 조회된다")
        @Test
        void findAll_success_returnsOnlyOrganizationDocuments() {
            // given
            documentRepository.save(
                    OrganizationDocumentFixture.create("org1", "content1", "writer1", 10L, UUID.randomUUID()));
            documentRepository.save(
                    OrganizationDocumentFixture.create("org2", "content2", "writer2", 15L, UUID.randomUUID()));

            // when
            List<Document> result = documentRepository.findAll();

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result).allMatch(doc -> doc.type() == DocumentType.ORGANIZATION);
            });
        }

        @DisplayName("문서가 여러개 존재했을 때 List 형태로 반환한다")
        @Test
        void findAll_success_bySomeData() {
            // given
            documentRepository.save(
                    DocumentFixture.createCrewDocument("title1", "content1", "writer1", 10L, UUID.randomUUID()));
            documentRepository.save(
                    DocumentFixture.createCrewDocument("title2", "content2", "writer2", 11L, UUID.randomUUID()));

            // when
            List<Document> crewDocuments = documentRepository.findAll();

            // then
            Assertions.assertThat(crewDocuments).hasSize(2);
        }

        @DisplayName("저장된 문서가 없을 때 빈 List를 반환한다")
        @Test
        void findAll_success_byNoData() {
            // when
            List<Document> crewDocuments = documentRepository.findAll();

            // then
            Assertions.assertThat(crewDocuments).hasSize(0);
        }
    }

    @Nested
    @DisplayName("uuid로 id 찾는 기능")
    class findIdByUuid {

        private UUID uuid;
        private CrewDocument savedCrewDocument;

        @BeforeEach
        void setUp() {
            uuid = UUID.randomUUID();

            CrewDocument crewDocument = DocumentFixture.createCrewDocument("titl1", "content1", "writer1", 10L, uuid);
            savedCrewDocument = documentRepository.save(crewDocument);

        }

        @DisplayName("존재하는 uuid로 찾을 시 Optional(id) 형태로 반환한다")
        @Test
        void findIdByUuid_success_byExistsUuid() {
            // when
            Optional<Long> actual = documentRepository.findIdByUuid(uuid);

            // then
            Assertions.assertThat(actual.get()).isEqualTo(savedCrewDocument.getId());
        }
    }

    @Nested
    @DisplayName("findAllByUuidIn 동작 확인")
    class FindAllByUuidIn {

        @DisplayName("UUID 리스트로 조회 시 해당 문서 리스트를 반환한다")
        @Test
        void findAllByUuidIn_success() {
            // given
            CrewDocument doc1 = documentRepository.save(
                    DocumentFixture.createCrewDocument("title1", "content1", "writer1", 10L, UUID.randomUUID()));
            CrewDocument doc2 = documentRepository.save(
                    DocumentFixture.createCrewDocument("title2", "content2", "writer2", 20L, UUID.randomUUID()));

            Set<UUID> uuids = Set.of(doc1.getUuid(), doc2.getUuid());

            // when
            List<Document> result = documentRepository.findAllByUuidIn(uuids);

            // then
            Assertions.assertThat(result).hasSize(2);
        }
    }
}
