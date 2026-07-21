package com.wooteco.wiki.document.controller;

import com.wooteco.wiki.document.dto.GenerationCrewResponse;
import com.wooteco.wiki.document.service.CrewDocumentQueryService;
import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponse.SuccessBody;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/document")
public class CrewDocumentQueryController {

    private final CrewDocumentQueryService crewDocumentQueryService;

    @Operation(summary = "기수별 크루 목록 조회", description = "기수에 속한 크루의 이름, 문서 UUID, 분야를 조회합니다.")
    @GetMapping("/crews")
    public ApiResponse<SuccessBody<List<GenerationCrewResponse>>> findAllByGeneration(
            @RequestParam Integer generation
    ) {
        List<GenerationCrewResponse> response = crewDocumentQueryService.findAllByGeneration(generation);
        return ApiResponseGenerator.success(response);
    }
}
