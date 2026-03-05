package com.wooteco.wiki.admin.controller;

import com.wooteco.wiki.admin.service.AdminService;
import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "문서 삭제", description = "문서 Uuid로 문서를 삭제합니다.")
    @DeleteMapping("/documents/{documentUuid}")
    public ApiResponse<ApiResponse.SuccessBody<Void>> deleteDocumentByDocumentId(@PathVariable UUID documentUuid) {
        adminService.deleteDocumentByDocumentUuid(documentUuid);
        return ApiResponseGenerator.success(HttpStatus.NO_CONTENT);
    }
}
