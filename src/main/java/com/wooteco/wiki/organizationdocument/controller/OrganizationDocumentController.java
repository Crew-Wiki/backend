package com.wooteco.wiki.organizationdocument.controller;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponse.SuccessBody;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentCreateRequest;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentUpdateRequest;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentAndEventResponse;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.service.OrganizationDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/organization")
@RestController
public class OrganizationDocumentController {

    private final OrganizationDocumentService organizationDocumentService;

    @Operation(summary = "UUID로 조직 문서 및 이벤트 조회", description = "UUID를 통해 조직 문서 및 이벤트를 조회합니다.")
    @GetMapping("uuid/{uuidText}")
    public ApiResponse<SuccessBody<OrganizationDocumentAndEventResponse>> get(@PathVariable UUID uuidText) {
        return ApiResponseGenerator.success(organizationDocumentService.findByUuid(uuidText));
    }

    @Operation(summary = "조직 위키 글 생성 및 연결", description = "조직 위키 글을 생성하며 크루 문서와 연결합니다.")
    @PostMapping
    public ApiResponse<SuccessBody<OrganizationDocumentResponse>> createOrganizationDocumentContents(
            @RequestBody OrganizationDocumentCreateRequest organizationDocumentCreateRequest) {
        OrganizationDocumentResponse organizationDocumentResponse = organizationDocumentService.create(
                organizationDocumentCreateRequest);

        return ApiResponseGenerator.success(organizationDocumentResponse);
    }

    @Operation(summary = "조직 위키 글 수정", description = "조직 위키 글을 수정합니다.")
    @PutMapping
    public ApiResponse<SuccessBody<OrganizationDocumentResponse>> modifyOrganizationDocumentContents(
            @RequestBody OrganizationDocumentUpdateRequest organizationDocumentModifyContentsRequest) {
        OrganizationDocumentResponse organizationDocumentResponse = organizationDocumentService.update(
                organizationDocumentModifyContentsRequest);

        return ApiResponseGenerator.success(organizationDocumentResponse);
    }
}
