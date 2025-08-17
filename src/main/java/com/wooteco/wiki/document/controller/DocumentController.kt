package com.wooteco.wiki.document.controller

import com.wooteco.wiki.document.domain.Document
import com.wooteco.wiki.document.domain.dto.*
import com.wooteco.wiki.document.service.DocumentSearchService
import com.wooteco.wiki.document.service.DocumentService
import com.wooteco.wiki.global.common.ApiResponse
import com.wooteco.wiki.global.common.ApiResponseGenerator
import com.wooteco.wiki.global.common.PageRequestDto
import com.wooteco.wiki.global.common.ResponseDto
import com.wooteco.wiki.log.domain.dto.LogDetailResponse
import com.wooteco.wiki.log.domain.dto.LogResponse
import com.wooteco.wiki.log.service.LogService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/document")
class DocumentController(
    private val documentService: DocumentService,
    private val logService: LogService,
    private val documentSearchService: DocumentSearchService,
) {

    @Operation(summary = "위키 글 작성", description = "위키 글을 작성합니다.")
    @PostMapping("")
    fun post(@RequestBody documentCreateRequest: DocumentCreateRequest): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.post(documentCreateRequest)
        return ApiResponseGenerator.success(response)
    }

    @Operation(summary = "랜덤 위키 글 조회", description = "랜덤으로 위키 글을 조회합니다.")
    @GetMapping("/random")
    fun getRandom(): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.getRandom()
        return ApiResponseGenerator.success(response)
    }

    @Operation(summary = "위키 글 전체 조회", description = "페이지네이션을 통해 모든 위키 글을 조회합니다.")
    @GetMapping("")
    fun findAll(@ModelAttribute pageRequestDto: PageRequestDto): ApiResponse<ApiResponse.SuccessBody<ResponseDto<List<Document>>>> {
        val pageResponses = documentService.findAll(pageRequestDto)
        return ApiResponseGenerator.success(convertToResponse(pageResponses))
    }

    @Operation(summary = "제목으로 위키 글 조회", description = "제목을 통해 위키 글을 조회합니다.")
    @GetMapping("title/{title}")
    fun get(@PathVariable title: String): ApiResponse<ApiResponse.SuccessBody<Any>> {
        val response = documentService.get(title)
        return ApiResponseGenerator.success(response)
    }

    @Operation(summary = "제목으로 UUID 조회", description = "제목을 통해 위키 글의 UUID를 조회합니다.")
    @GetMapping("title/{title}/uuid")
    fun getUuidByTitle(@PathVariable title: String): ApiResponse<ApiResponse.SuccessBody<Any>> {
        val response = documentService.getUuidByTitle(title)
        return ApiResponseGenerator.success(response)
    }

    @Operation(summary = "UUID로 위키 글 조회", description = "UUID를 통해 위키 글을 조회합니다.")
    @GetMapping("uuid/{uuidText}")
    fun getByUuid(@PathVariable uuidText: String): ApiResponse<ApiResponse.SuccessBody<Any>> {
        val uuid = UUID.fromString(uuidText)
        val response = documentService.getByUuid(uuid)
        return ApiResponseGenerator.success(response)
    }

    @Operation(summary = "문서 로그 목록 조회", description = "문서 UUID로 해당 문서의 로그 목록을 페이지네이션을 통해 조회합니다.")
    @GetMapping("uuid/{uuidText}/log")
    fun getLogs(
        @PathVariable uuidText: String,
        @ModelAttribute pageRequestDto: PageRequestDto
    ): ApiResponse<ApiResponse.SuccessBody<ResponseDto<List<LogResponse>>>> {
        val uuid = UUID.fromString(uuidText)
        val pageResponses = logService.findAllByDocumentUuid(uuid, pageRequestDto)
        return ApiResponseGenerator.success(convertToResponse(pageResponses))
    }

    @Operation(summary = "로그 상세 조회", description = "로그 ID로 로그 상세 정보를 조회합니다.")
    @GetMapping("/log/{logId}")
    fun getDocumentLogs(@PathVariable logId: Long): ApiResponse<ApiResponse.SuccessBody<LogDetailResponse>> {
        val logDetail = logService.getLogDetail(logId)
        return ApiResponseGenerator.success(logDetail)
    }

    @Operation(summary = "위키 글 수정", description = "위키 글을 수정합니다.")
    @PutMapping
    fun put(
        @RequestBody documentUpdateRequest: DocumentUpdateRequest
    ): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.put(documentUpdateRequest.uuid, documentUpdateRequest)
        return ApiResponseGenerator.success(response)
    }

    @Operation(summary = "키워드로 위키 글 검색", description = "키워드로 위키 글을 검색합니다.")
    @GetMapping("/search")
    fun search(@RequestParam keyWord: String): ApiResponse<ApiResponse.SuccessBody<List<DocumentSearchResponse>>> {
        return ApiResponseGenerator.success(documentSearchService.search(keyWord))
    }

    @Operation(summary = "누적 조회수 수신 API", description = "프론트에서 누적된 조회수를 전달받아 DB에 반영합니다.")
    @PostMapping("/views/flush")
    fun flushViews(
        @RequestBody request: ViewFlushRequest
    ): ApiResponse<ApiResponse.SuccessBody<String>> {
        documentService.flushViews(request.views)
        return ApiResponseGenerator.success("조회수 누적 완료")
    }

    private fun <T> convertToResponse(pageResponses: Page<T>): ResponseDto<List<T>> {
        return ResponseDto.of(
            pageResponses.number,
            pageResponses.totalPages,
            pageResponses.content
        )
    }
}
