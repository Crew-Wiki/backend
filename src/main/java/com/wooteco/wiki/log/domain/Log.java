package com.wooteco.wiki.log.domain;

import com.wooteco.wiki.document.domain.Document;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    private String title;
    private String contents;
    private String writer;

    @Column(name = "document_bytes")
    private Long documentBytes;

    @Column(name = "generate_time")
    private LocalDateTime generateTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Document document;

    protected Log() {
    }

    public Log(String title, String contents, String writer, long documentBytes, LocalDateTime generateTime,
               Document document) {
        this.title = title;
        this.contents = contents;
        this.writer = writer;
        this.documentBytes = documentBytes;
        this.generateTime = generateTime;
        this.document = document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Log that)) {
            return false;
        }
        if (this.id == null || that.id == null) {
            return false;
        }
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
