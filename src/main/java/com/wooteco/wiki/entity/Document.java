package com.wooteco.wiki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;
    @Column(unique = true)
    private String title;
    private String contents;
    private String writer;
    private LocalDateTime generateTime;

    public void update(String contents, String writer, LocalDateTime generateTime) {
        this.contents = contents;
        this.writer = writer;
        this.generateTime = generateTime;
    }
}
