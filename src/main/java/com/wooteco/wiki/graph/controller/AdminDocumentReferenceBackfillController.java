package com.wooteco.wiki.graph.controller;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponse.SuccessBody;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import com.wooteco.wiki.graph.dto.AdminDocumentReferenceBackfillResponse;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillResult;
import com.wooteco.wiki.graph.service.DocumentReferenceBackfillService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/graph/document-references")
public class AdminDocumentReferenceBackfillController {

    private final DocumentReferenceBackfillService documentReferenceBackfillService;

    @Operation(
            summary = "문서 참조 backfill",
            description = "모든 크루 문서 본문을 다시 파싱해 document_reference를 동기화합니다. "
                    + "두 번 실행해 added와 removed가 모두 0이면 영속 참조 읽기 경로로 전환할 수 있습니다."
    )
    @PostMapping("/backfill")
    public ApiResponse<SuccessBody<AdminDocumentReferenceBackfillResponse>> backfill() {
        DocumentReferenceBackfillResult result = documentReferenceBackfillService.backfill();
        return ApiResponseGenerator.success(AdminDocumentReferenceBackfillResponse.from(result));
    }
}
