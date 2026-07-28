package com.wooteco.wiki.history.repository;

import com.wooteco.wiki.history.domain.History;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoryRepository extends JpaRepository<History, Long> {

    @Query("SELECT h FROM History h WHERE h.document.id = :documentId")
    Page<History> findAllByDocumentId(
            Long documentId,
            Pageable pageable
    );

    @Query("SELECT MAX(h.version) FROM History h WHERE h.document.id = :documentId")
    Optional<Long> findMaxVersionByDocumentId(@Param("documentId") Long documentId);
}

