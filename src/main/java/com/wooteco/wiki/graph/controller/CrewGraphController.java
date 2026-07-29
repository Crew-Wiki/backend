package com.wooteco.wiki.graph.controller;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponse.SuccessBody;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.service.CrewGraphQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/graph")
public class CrewGraphController {

    private final CrewGraphQueryService crewGraphQueryService;

    @Operation(summary = "크루 관계 그래프 조회", description = "기수에 속한 크루 문서 노드와 관계를 조회합니다.")
    @GetMapping
    public ApiResponse<SuccessBody<CrewGraphResponse>> findByGeneration(
            @RequestParam String generation
    ) {
        CrewGraphResponse response = crewGraphQueryService.findByGeneration(generation);
        return ApiResponseGenerator.success(response);
    }
}
