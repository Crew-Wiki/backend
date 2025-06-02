package com.wooteco.wiki.repository;

import com.wooteco.wiki.admin.domain.Admin;
import com.wooteco.wiki.admin.repository.AdminRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Nested
    @DisplayName("loginId와 password로 Admin을 찾는 기능")
    class findOneByLoginIdAndPassword {

        @DisplayName("유효한 loginId와 password로 Admin을 반환한다.")
        @Test
        void findOneByLoginIdAndPassword_success_byValidLoginIdAndPassword() {
            // given
            Admin savedAdmin = adminRepository.save(new Admin("admin", "password"));

            // when
            Optional<Admin> adminOptional = adminRepository.findOneByLoginIdAndPassword(
                    savedAdmin.getLoginId(), savedAdmin.getPassword());

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(adminOptional.isPresent()).isTrue();
                softly.assertThat(adminOptional.get().getLoginId()).isEqualTo(savedAdmin.getLoginId());
                softly.assertThat(adminOptional.get().getPassword()).isEqualTo(savedAdmin.getPassword());
            });
        }

        @DisplayName("알맞지 않는 loginId와 password로 Optional.isEmpty를 반환한다.")
        @Test
        void findOneByLoginIdAndPassword_isEmpty_byInValidLoginIdAndPassword() {
            // given
            adminRepository.save(new Admin("admin", "password"));

            // when
            Optional<Admin> adminOptional = adminRepository.findOneByLoginIdAndPassword("invalidLoginId", "invalidPassword");

            // then
            Assertions.assertThat(adminOptional.isEmpty()).isTrue();
        }
    }

}
