package com.wooteco.wiki.repository;

import com.wooteco.wiki.entity.Document;
import com.wooteco.wiki.entity.Title;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByTitle(String title);

    boolean existsByTitle(String title);

    List<Document> findAllByOrderByGenerateTimeDesc();

    List<Title> findAllByTitleStartingWith(String keyWord);
}
