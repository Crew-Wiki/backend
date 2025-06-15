package com.wooteco.wiki.document.controller

import com.wooteco.wiki.document.domain.Document
import com.wooteco.wiki.document.domain.dto.*
import com.wooteco.wiki.document.service.DocumentSearchService
import com.wooteco.wiki.document.service.DocumentService
import com.wooteco.wiki.document.service.UUIDService
import com.wooteco.wiki.global.common.ApiResponse
import com.wooteco.wiki.global.common.ApiResponseGenerator
import com.wooteco.wiki.global.common.PageRequestDto
import com.wooteco.wiki.global.common.ResponseDto
import com.wooteco.wiki.log.domain.dto.LogDetailResponse
import com.wooteco.wiki.log.domain.dto.LogResponse
import com.wooteco.wiki.log.service.LogService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/document")
class DocumentController(
    private val documentService: DocumentService,
    private val logService: LogService,
    private val documentSearchService: DocumentSearchService,
    private val uuidService: UUIDService,
) {

    @PostMapping("")
    fun post(@RequestBody documentCreateRequest: DocumentCreateRequest): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.post(documentCreateRequest)
        return ApiResponseGenerator.success(response, HttpStatus.CREATED, "문서가 생성되었습니다.")
    }

    @GetMapping("/random")
    fun getRandom(): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.getRandom()
        return ApiResponseGenerator.success(response, HttpStatus.OK, "랜덤 문서를 조회했습니다.")
    }

    @GetMapping("")
    fun findAll(@ModelAttribute pageRequestDto: PageRequestDto): ApiResponse<ApiResponse.SuccessBody<ResponseDto<List<Document>>>> {
        val pageResponses = documentService.findAll(pageRequestDto)
        val response = ResponseDto.of(
            pageResponses.number,
            pageResponses.totalPages,
            pageResponses.content
        )
        return ApiResponseGenerator.success(response, HttpStatus.OK, "문서 목록을 조회했습니다.")
    }

    @GetMapping("title/{title}")
    fun get(@PathVariable title: String): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.get(title)
        return ApiResponseGenerator.success(response, HttpStatus.OK, "문서를 조회했습니다.")
    }

    @GetMapping("title/{title}/uuid")
    fun getUuidByTitle(@PathVariable title: String): ApiResponse<ApiResponse.SuccessBody<DocumentUuidResponse>> {
        val response = documentService.getUuidByTitle(title)
        return ApiResponseGenerator.success(response, HttpStatus.OK, "문서의 UUID를 조회했습니다.")
    }

    @GetMapping("uuid/{uuidText}")
    fun getByUuid(@PathVariable uuidText: String): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val uuid = UUID.fromString(uuidText)
        val response = documentService.getByUuid(uuid)
        return ApiResponseGenerator.success(response, HttpStatus.OK, "UUID로 문서를 조회했습니다.")
    }

    @GetMapping("/{title}/log")
    fun getLogs(@PathVariable title: String): ApiResponse<ApiResponse.SuccessBody<List<LogResponse>>> {
        val response = logService.getLogs(title)
        return ApiResponseGenerator.success(response, HttpStatus.OK, "문서의 로그를 조회했습니다.")
    }

    @GetMapping("/log/{logId}")
    fun getDocumentLogs(@PathVariable logId: Long): ApiResponse<ApiResponse.SuccessBody<LogDetailResponse>> {
        val logDetail = logService.getLogDetail(logId)
        return ApiResponseGenerator.success(logDetail, HttpStatus.OK, "로그 상세 정보를 조회했습니다.")
    }

    @PutMapping("/{uuidText}")
    fun put(
        @PathVariable uuidText: String,
        @RequestBody documentUpdateRequest: DocumentUpdateRequest
    ): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.put(uuidText, documentUpdateRequest)
        return ApiResponseGenerator.success(response, HttpStatus.OK, "문서가 수정되었습니다.")
    }

    @GetMapping("/search")
    fun search(@RequestParam keyWord: String): ApiResponse<ApiResponse.SuccessBody<List<DocumentSearchResponse>>> {
        val response = documentSearchService.search(keyWord)
        return ApiResponseGenerator.success(response, HttpStatus.OK, "문서 검색이 완료되었습니다.")
    }

    @GetMapping("/uuid")
    fun getUUID(): ApiResponse<ApiResponse.SuccessBody<DocumentUuidResponse>> {
        val uuid = uuidService.generate()
        return ApiResponseGenerator.success(DocumentUuidResponse(uuid), HttpStatus.OK, "UUID가 생성되었습니다.")
    }
}
