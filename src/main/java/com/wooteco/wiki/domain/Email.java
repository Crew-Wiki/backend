package com.wooteco.wiki.domain;

import static com.wooteco.wiki.exception.ExceptionType.EMAIL_INVALID;

import com.wooteco.wiki.exception.WikiException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

@Embeddable
@Getter
public class Email {
    private static final String EMAIL_FORMAT = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_FORMAT);
    @Column(name = "email")
    private String rawEmail;

    public Email(String rawEmail) {
        validateEmail(rawEmail);
        this.rawEmail = rawEmail;
    }

    private void validateEmail(String rawEmail) {
        Matcher matcher = PATTERN.matcher(rawEmail);
        if (!matcher.matches()) {
            throw new WikiException(EMAIL_INVALID);
        }
    }

    protected Email() {

    }
}
