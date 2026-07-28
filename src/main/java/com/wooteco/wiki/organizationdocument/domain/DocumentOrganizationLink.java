package com.wooteco.wiki.organizationdocument.domain;

import com.wooteco.wiki.document.domain.CrewDocument;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "document_orgdoc_link",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_doc_orgdoc", columnNames = {"document_id", "organization_document_id"}
        )
)
public class DocumentOrganizationLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_link_doc"))
    private CrewDocument crewDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_document_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_link_orgdoc"))
    private OrganizationDocument organizationDocument;

    public DocumentOrganizationLink(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    ) {
        this.crewDocument = crewDocument;
        this.organizationDocument = organizationDocument;
    }
}
