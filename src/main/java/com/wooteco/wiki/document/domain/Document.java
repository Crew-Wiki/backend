package com.wooteco.wiki.document.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", length = 20)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false, unique = true)
    protected String title = "";

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    protected String contents = "";

    protected String writer = "";

    protected Long documentBytes = 0L;

    protected LocalDateTime generateTime = LocalDateTime.now();

    @JdbcTypeCode(Types.CHAR)
    protected UUID uuid = UUID.randomUUID();

    @Column(name = "view_count", nullable = false, columnDefinition = "INT DEFAULT 0 NOT NULL")
    protected Integer viewCount = 0;

    public Document(final String title, final String contents, final String writer,
                    final Long documentBytes, final UUID uuid) {
        this.title = title;
        this.contents = contents;
        this.writer = writer;
        this.documentBytes = documentBytes;
        this.uuid = uuid;
    }

    public Document update(
            String title,
            String contents,
            String writer,
            Long documentBytes,
            LocalDateTime generateTime
    ) {
        this.title = title;
        this.contents = contents;
        this.writer = writer;
        this.documentBytes = documentBytes;
        this.generateTime = generateTime;
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Document)) {
            return false;
        }
        Document that = (Document) other;
        return this.id != null && this.id.equals(that.id);
    }

    public void changeViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public abstract DocumentType type();

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
