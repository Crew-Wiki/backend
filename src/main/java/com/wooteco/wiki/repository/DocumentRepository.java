package com.wooteco.wiki.repository;

import com.wooteco.wiki.entity.Document;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByTitle(String title);
}
