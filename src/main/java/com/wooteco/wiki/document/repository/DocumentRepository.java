package com.wooteco.wiki.document.repository;

import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.dto.DocumentTitleListResponse;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByTitle(String title);

    Boolean existsByTitle(String title);

    List<Document> findAllByTitleStartingWithOrderByTitle(String keyWord);

    @Query("""
        SELECT new com.wooteco.wiki.document.domain.dto.DocumentTitleListResponse(d.title, d.uuid)
        FROM Document d
        """)
    List<DocumentTitleListResponse> findAllTitles();

    Optional<Document> findByUuid(UUID uuid);

    @Query("SELECT d.uuid FROM Document d WHERE d.title = :title")
    Optional<UUID> findUuidByTitle(@Param("title") String title);

    @Query("SELECT d.id FROM Document d WHERE d.uuid = :uuid")
    Optional<Long> findIdByUuid(@Param("uuid") UUID documentUuid);

    List<Document> findAllByUuidIn(Set<UUID> uuids);

    void deleteByUuid(UUID documentUuid);
}
