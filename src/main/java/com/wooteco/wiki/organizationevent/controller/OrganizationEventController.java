package com.wooteco.wiki.organizationevent.controller;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponse.SuccessBody;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import com.wooteco.wiki.organizationevent.dto.request.OrganizationEventCreateRequest;
import com.wooteco.wiki.organizationevent.dto.request.OrganizationEventUpdateRequest;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventCreateResponse;
import com.wooteco.wiki.organizationevent.dto.response.OrganizationEventUpdateResponse;
import com.wooteco.wiki.organizationevent.service.OrganizationEventService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/organization-events")
public class OrganizationEventController {

    private final OrganizationEventService organizationEventService;

    @Operation(summary = "조직 이벤트 생성", description = "조직 이벤트를 생성합니다.")
    @PostMapping
    public ApiResponse<SuccessBody<OrganizationEventCreateResponse>> post(
            @RequestBody @Valid OrganizationEventCreateRequest organizationEventCreateRequest) {
        OrganizationEventCreateResponse response = organizationEventService.post(organizationEventCreateRequest);
        return ApiResponseGenerator.success(response, HttpStatus.CREATED);
    }

    @Operation(summary = "조직 이벤트 수정", description = "조직 이벤트를 수정합니다.")
    @PutMapping("/{organizationEventUuid}")
    public ApiResponse<SuccessBody<OrganizationEventUpdateResponse>> put(
            @PathVariable UUID organizationEventUuid,
            @RequestBody @Valid OrganizationEventUpdateRequest organizationEventUpdateRequest
    ) {
        OrganizationEventUpdateResponse response = organizationEventService.put(organizationEventUuid,
                organizationEventUpdateRequest);
        return ApiResponseGenerator.success(response);
    }

    @Operation(summary = "조직 이벤트 삭제", description = "조직 이벤트를 삭제합니다.")
    @DeleteMapping("/{organizationEventUuid}")
    public ApiResponse<SuccessBody<Void>> delete(
            @PathVariable UUID organizationEventUuid) {
        organizationEventService.delete(organizationEventUuid);
        return ApiResponseGenerator.success(HttpStatus.NO_CONTENT);
    }
}
