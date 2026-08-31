package com.wooteco.wiki.document.repository;

import com.wooteco.wiki.document.domain.CrewDocument;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewDocumentRepository extends JpaRepository<CrewDocument, Long> {

    Optional<CrewDocument> findByUuid(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT crewDocument FROM CrewDocument crewDocument WHERE crewDocument.uuid = :uuid")
    Optional<CrewDocument> findByUuidForUpdate(@Param("uuid") UUID uuid);

    @Query("""
        SELECT new com.wooteco.wiki.document.repository.CrewDocumentIdentifierReadModel(
            crewDocument.id,
            crewDocument.uuid
        )
        FROM CrewDocument crewDocument
        WHERE crewDocument.id > :lastDocumentId
        ORDER BY crewDocument.id ASC
        """)
    List<CrewDocumentIdentifierReadModel> findIdentifiersAfterId(
            @Param("lastDocumentId") Long lastDocumentId,
            Pageable pageable
    );
}
