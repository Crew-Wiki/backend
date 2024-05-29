package com.wooteco.wiki.domain;

import com.wooteco.wiki.exception.WrongPasswordException;
import com.wooteco.wiki.util.Sha256Encryptor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

@Embeddable
@Getter
public class Password {
    private static final String PASSWORD_REGEX = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=|{}\\[\\]:;\"',.?/~`]).{8,20}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_REGEX);
    @Column(name = "password")
    private String rawPassword;

    public Password(String rawPassword) {
        validatePassword(rawPassword);
        this.rawPassword = Sha256Encryptor.encrypt(rawPassword);
    }

    private static void validatePassword(String rawPassword) {
        Matcher matcher = PATTERN.matcher(rawPassword);
        if (!matcher.matches()) {
            throw new WrongPasswordException("잘못된 비밀번호입니다.");
        }
    }

    protected Password() {

    }
}
