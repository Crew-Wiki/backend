package com.wooteco.wiki.global.auth.domain.dto;

import com.wooteco.wiki.admin.domain.Admin;
import com.wooteco.wiki.global.auth.Role;

public record TokenInfoResponse (Long id, Role role) {

    public static TokenInfoResponse of(Admin admin, Role role) {
        return new TokenInfoResponse(admin.getId(), role);
    }
}
