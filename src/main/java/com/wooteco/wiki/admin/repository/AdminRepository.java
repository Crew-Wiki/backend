package com.wooteco.wiki.admin.repository;

import com.wooteco.wiki.admin.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findOneByLoginIdAndPassword(String loginId, String password);
}
