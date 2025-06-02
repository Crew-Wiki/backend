package com.wooteco.wiki.admin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Admin {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loginId;
    private String password;

    protected Admin() {
    }

    public Admin(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }
}
