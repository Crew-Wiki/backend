package com.wooteco.wiki.service;

import static com.wooteco.wiki.domain.MemberState.INITIAL;
import static com.wooteco.wiki.domain.MemberState.WAITING;

import com.wooteco.wiki.domain.Email;
import com.wooteco.wiki.domain.Member;
import com.wooteco.wiki.domain.Password;
import com.wooteco.wiki.domain.TokenManager;
import com.wooteco.wiki.dto.AuthTokens;
import com.wooteco.wiki.dto.JoinRequest;
import com.wooteco.wiki.dto.LoginRequest;
import com.wooteco.wiki.exception.DuplicateEmailException;
import com.wooteco.wiki.exception.MemberNotFoundException;
import com.wooteco.wiki.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final TokenManager tokenManager;
    private final MemberRepository memberRepository;

    public AuthTokens login(LoginRequest loginRequest) {
        Email email = new Email(loginRequest.email());
        Password password = new Password(loginRequest.password());
        Member member = memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new MemberNotFoundException("아이디 혹은 비밀번호가 잘못되었습니다."));
        return generateAuthTokens(member);
    }

    private AuthTokens generateAuthTokens(Member member) {
        String accessToken = tokenManager.generateAccessToken(member);
        String refreshToken = tokenManager.generateRefreshToken(member);
        return new AuthTokens(accessToken, refreshToken);
    }

    public AuthTokens join(JoinRequest joinRequest) {
        String nickname = joinRequest.nickname();
        Member member = memberRepository.findByNicknameAndState(nickname, INITIAL)
                .orElseGet(() -> makeNewMember(joinRequest));
        member.updateEmailAndPasswordWhenINITIAL(joinRequest.email(), joinRequest.password());
        memberRepository.saveAndFlush(member);
        return generateAuthTokens(member);
    }

    private Member makeNewMember(JoinRequest joinRequest) {
        if (memberRepository.findByEmail(new Email(joinRequest.email())).isPresent()) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }
        return Member.builder()
                .email(joinRequest.email())
                .password(joinRequest.password())
                .state(WAITING)
                .nickname(joinRequest.nickname())
                .build();
    }
}
