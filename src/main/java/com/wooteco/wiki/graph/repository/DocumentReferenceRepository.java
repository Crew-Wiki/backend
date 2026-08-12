package com.wooteco.wiki.graph.repository;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.graph.domain.DocumentReference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentReferenceRepository extends JpaRepository<DocumentReference, Long> {

    List<DocumentReference> findAllBySourceDocument(CrewDocument sourceDocument);

    void deleteAllBySourceDocument(CrewDocument sourceDocument);

    void deleteAllByTargetDocument(Document targetDocument);
}
