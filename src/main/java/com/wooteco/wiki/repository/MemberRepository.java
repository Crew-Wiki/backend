package com.wooteco.wiki.repository;

import com.wooteco.wiki.domain.Email;
import com.wooteco.wiki.domain.Member;
import com.wooteco.wiki.domain.MemberState;
import com.wooteco.wiki.domain.Password;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(Email email);

    Optional<Member> findByEmailAndPassword(Email email, Password password);

    Optional<Member> findByNicknameAndState(String nickName, MemberState state);
}
