package com.wooteco.wiki.log.repository;

import com.wooteco.wiki.log.domain.Log;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findAllByTitleOrderByLogIdAsc(String title);
}
