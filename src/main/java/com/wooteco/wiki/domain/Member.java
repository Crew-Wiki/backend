package com.wooteco.wiki.domain;

import static com.wooteco.wiki.domain.MemberState.INITIAL;
import static com.wooteco.wiki.domain.MemberState.WAITING;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    private String nickname;
    private Email email;
    @Embedded
    private Password password;
    @Enumerated(EnumType.STRING)
    private MemberState state;

    @Builder
    public Member(Long memberId, String nickname, String email, String password, MemberState state) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.email = new Email(email);
        this.password = new Password(password);
        this.state = state;
    }

    public void updateEmailAndPasswordWhenINITIAL(String email, String password) {
        if (INITIAL.equals(state)) {
            this.email = new Email(email);
            this.password = new Password(password);
            state = WAITING;
        }
    }

    public String getRawEmail() {
        return email.getRawEmail();
    }

    private String getRawPassword() {
        return password.getRawPassword();
    }
}
