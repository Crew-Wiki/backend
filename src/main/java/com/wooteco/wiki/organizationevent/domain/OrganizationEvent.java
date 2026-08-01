package com.wooteco.wiki.organizationevent.domain;

import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "organization_event")
public class OrganizationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String contents;

    private String writer;

    private LocalDate occurredAt;

    @JdbcTypeCode(Types.CHAR)
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_document_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_orgdoc"))
    private OrganizationDocument organizationDocument;

    public static OrganizationEvent create(
            String title,
            String contents,
            String writer,
            LocalDate occurredAt,
            OrganizationDocument organizationDocument
    ) {
        return OrganizationEvent.builder()
                .uuid(UUID.randomUUID())
                .title(title.trim())
                .contents(getContents(contents))
                .writer(writer.trim())
                .occurredAt(occurredAt)
                .organizationDocument(organizationDocument)
                .build();
    }

    public void update(
            String title,
            String contents,
            String writer,
            LocalDate occurredAt
    ) {
        this.title = title.trim();
        this.contents = getContents(contents);
        this.writer = writer.trim();
        this.occurredAt = occurredAt;
    }

    private static String getContents(String contents) {
        return contents == null ? "" : contents;
    }
}
