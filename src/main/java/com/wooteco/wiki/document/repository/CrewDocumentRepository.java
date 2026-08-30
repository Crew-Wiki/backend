package com.wooteco.wiki.document.repository;

import com.wooteco.wiki.document.domain.CrewDocument;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CrewDocumentRepository extends JpaRepository<CrewDocument, Long> {

    Optional<CrewDocument> findByUuid(UUID uuid);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT crewDocument FROM CrewDocument crewDocument WHERE crewDocument.uuid = :uuid")
    Optional<CrewDocument> findByUuidForUpdate(@Param("uuid") UUID uuid);
}
