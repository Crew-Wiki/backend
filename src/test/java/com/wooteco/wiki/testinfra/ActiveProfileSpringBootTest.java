package com.wooteco.wiki.testinfra;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = {"local", "info-logging"})
public class ActiveProfileSpringBootTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDB() {
        jdbcTemplate.update("DELETE FROM document");
        jdbcTemplate.update("ALTER TABLE document ALTER COLUMN document_id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM log");
        jdbcTemplate.update("ALTER TABLE log ALTER COLUMN log_id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM member");
        jdbcTemplate.update("ALTER TABLE member ALTER COLUMN member_id RESTART WITH 1");
    }
}
