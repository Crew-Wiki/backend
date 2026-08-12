package com.wooteco.wiki.graph.domain;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "document_reference",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_document_reference_source_target",
                columnNames = {"source_document_id", "target_document_id"}
        ),
        indexes = {
                @Index(name = "idx_document_reference_source", columnList = "source_document_id"),
                @Index(name = "idx_document_reference_target", columnList = "target_document_id")
        }
)
public class DocumentReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_document_reference_source")
    )
    private CrewDocument sourceDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "target_document_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_document_reference_target")
    )
    private Document targetDocument;

    public static DocumentReference of(
            CrewDocument sourceDocument,
            Document targetDocument
    ) {
        return new DocumentReference(sourceDocument, targetDocument);
    }

    private DocumentReference(
            CrewDocument sourceDocument,
            Document targetDocument
    ) {
        this.sourceDocument = sourceDocument;
        this.targetDocument = targetDocument;
    }

    public CrewDocument getSourceDocument() {
        return sourceDocument;
    }

    public Document getTargetDocument() {
        return targetDocument;
    }
}
