package com.wooteco.wiki.document.controller;

import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.dto.*;
import com.wooteco.wiki.document.service.DocumentSearchService;
import com.wooteco.wiki.document.service.DocumentService;
import com.wooteco.wiki.document.service.DocumentServiceJava;
import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponse.SuccessBody;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import com.wooteco.wiki.global.common.PageRequestDto;
import com.wooteco.wiki.global.common.ResponseDto;
import com.wooteco.wiki.history.domain.dto.HistoryDetailResponse;
import com.wooteco.wiki.history.domain.dto.HistoryResponse;
import com.wooteco.wiki.history.service.HistoryService;
import com.wooteco.wiki.organizationdocument.dto.OrganizationDocumentSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final HistoryService historyService;
    private final DocumentSearchService documentSearchService;
    private final DocumentServiceJava documentServiceJava;

    @Operation(summary = "위키 글 작성", description = "위키 글을 작성합니다.")
    @PostMapping
    public ApiResponse<SuccessBody<DocumentResponse>> post(@RequestBody CrewDocumentCreateRequest crewDocumentCreateRequest) {
        DocumentResponse response = documentService.postCrewDocument(crewDocumentCreateRequest);
        return ApiResponseGenerator.success(response);
    }

    @Operation(summary = "랜덤 위키 글 조회", description = "랜덤으로 위키 글을 조회합니다.")
    @GetMapping("/random")
    public ApiResponse<SuccessBody<DocumentResponse>> getRandom() {
        DocumentResponse response = documentService.getRandom();
        return ApiResponseGenerator.success(response);
    }

    @Operation(summary = "위키 글 전체 조회", description = "페이지네이션을 통해 모든 위키 글을 조회합니다.")
    @GetMapping("")
    public ApiResponse<SuccessBody<ResponseDto<List<DocumentListResponse>>>> findAll(@ModelAttribute PageRequestDto pageRequestDto) {
        Page<Document> pageResponses = documentService.findAll(pageRequestDto);
        Page<DocumentListResponse> responses = pageResponses.map(DocumentListResponse::from);
        return ApiResponseGenerator.success(convertToResponse(responses));
    }

    @Operation(summary = "제목으로 위키 글 조회", description = "제목을 통해 위키 글을 조회합니다.")
    @GetMapping("title/{title}")
    public ApiResponse<SuccessBody<Object>> get(@PathVariable String title) {
        Object response = documentService.get(title);
        return ApiResponseGenerator.success(response);
    }

    @Operation(summary = "제목으로 UUID 조회", description = "제목을 통해 위키 글의 UUID를 조회합니다.")
    @GetMapping("title/{title}/uuid")
    public ApiResponse<SuccessBody<Object>> getUuidByTitle(@PathVariable String title) {
        Object response = documentService.getUuidByTitle(title);
        return ApiResponseGenerator.success(response);
    }

    @Operation(summary = "UUID로 위키 글 조회", description = "UUID를 통해 위키 글을 조회합니다.")
    @GetMapping("uuid/{uuidText}")
    public ApiResponse<SuccessBody<Object>> getByUuid(@PathVariable String uuidText) {
        UUID uuid = UUID.fromString(uuidText);
        Object response = documentService.getByUuid(uuid);
        return ApiResponseGenerator.success(response);
    }

    @Operation(summary = "문서 로그 목록 조회", description = "문서 UUID로 해당 문서의 로그 목록을 페이지네이션을 통해 조회합니다.")
    @GetMapping("uuid/{uuidText}/log")
    public ApiResponse<SuccessBody<ResponseDto<List<HistoryResponse>>>> getLogs(
            @PathVariable String uuidText,
            @ModelAttribute PageRequestDto pageRequestDto
    ) {
        UUID uuid = UUID.fromString(uuidText);
        Page<HistoryResponse> pageResponses = historyService.findAllByDocumentUuid(uuid, pageRequestDto);
        return ApiResponseGenerator.success(convertToResponse(pageResponses));
    }

    @Operation(summary = "로그 상세 조회", description = "로그 ID로 로그 상세 정보를 조회합니다.")
    @GetMapping("/log/{logId}")
    public ApiResponse<SuccessBody<HistoryDetailResponse>> getDocumentLogs(@PathVariable Long logId) {
        HistoryDetailResponse logDetail = historyService.getLogDetail(logId);
        return ApiResponseGenerator.success(logDetail);
    }

    @Operation(summary = "위키 글 수정", description = "위키 글을 수정합니다.")
    @PutMapping
    public ApiResponse<SuccessBody<DocumentResponse>> put(
            @RequestBody DocumentUpdateRequest documentUpdateRequest
    ) {
        DocumentResponse response = documentService.put(documentUpdateRequest.uuid(), documentUpdateRequest);
        return ApiResponseGenerator.success(response);
    }

    @Operation(summary = "키워드로 위키 글 검색", description = "키워드로 위키 글을 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<SuccessBody<List<DocumentSearchResponse>>> search(@RequestParam String keyWord) {
        return ApiResponseGenerator.success(documentSearchService.search(keyWord));
    }

    @Operation(summary = "누적 조회수 수신 API", description = "프론트에서 누적된 조회수를 전달받아 DB에 반영합니다.")
    @PostMapping("/views/flush")
    public ApiResponse<SuccessBody<String>> flushViews(
            @RequestBody ViewFlushRequest request
    ) {
        documentService.flushViews(request.views());
        return ApiResponseGenerator.success("조회수 누적 완료");
    }

    @Operation(summary = "특정 문서에 대한 조직 문서 조회 API", description = "특정 문서에 대한 조직 문서들을 조회합니다.")
    @GetMapping("/{uuidText}/organization-documents")
    public ApiResponse<SuccessBody<List<OrganizationDocumentSearchResponse>>> readOrganizationDocument(
            @PathVariable String uuidText
    ) {
        UUID uuid = UUID.fromString(uuidText);
        return ApiResponseGenerator.success(documentServiceJava.searchOrganizationDocument(uuid));
    }

    @Operation(summary = "특정 문서에 대한 조직 문서 삭제 API", description = "특정 문서에 대한 조직 문서를 제거합니다.")
    @DeleteMapping("/{uuidText}/organization-documents/{organizationDocumentUuidText}")
    public ApiResponse<SuccessBody<Void>> deleteOrganizationDocument(
            @PathVariable String uuidText,
            @PathVariable String organizationDocumentUuidText
    ) {
        UUID documentUuid = UUID.fromString(uuidText);
        UUID organizationDocumentUuid = UUID.fromString(organizationDocumentUuidText);
        documentServiceJava.deleteOrganizationDocument(documentUuid, organizationDocumentUuid);
        return ApiResponseGenerator.success(HttpStatus.NO_CONTENT);
    }

    private <T> ResponseDto<List<T>> convertToResponse(Page<T> pageResponses) {
        return ResponseDto.of(
                pageResponses.getNumber(),
                pageResponses.getTotalPages(),
                pageResponses.getContent()
        );
    }
}
