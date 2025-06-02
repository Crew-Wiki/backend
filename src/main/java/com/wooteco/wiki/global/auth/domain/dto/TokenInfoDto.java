package com.wooteco.wiki.global.auth.domain.dto;

import com.wooteco.wiki.admin.domain.Admin;

public record TokenInfoDto (Long id) {

    public static TokenInfoDto of(Admin admin) {
        return new TokenInfoDto(admin.getId());
    }
}
