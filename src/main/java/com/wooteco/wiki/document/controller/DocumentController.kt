package com.wooteco.wiki.document.controller

import com.wooteco.wiki.document.domain.Document
import com.wooteco.wiki.document.domain.dto.DocumentCreateRequest
import com.wooteco.wiki.document.domain.dto.DocumentResponse
import com.wooteco.wiki.document.domain.dto.DocumentSearchResponse
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest
import com.wooteco.wiki.document.service.DocumentSearchService
import com.wooteco.wiki.document.service.DocumentService
import com.wooteco.wiki.global.common.ApiResponse
import com.wooteco.wiki.global.common.ApiResponseGenerator
import com.wooteco.wiki.global.common.PageRequestDto
import com.wooteco.wiki.global.common.ResponseDto
import com.wooteco.wiki.log.domain.dto.LogDetailResponse
import com.wooteco.wiki.log.domain.dto.LogResponse
import com.wooteco.wiki.log.service.LogService
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

    @PostMapping("")
    fun post(@RequestBody documentCreateRequest: DocumentCreateRequest): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.post(documentCreateRequest)
        return ApiResponseGenerator.success(response)
    }

    @GetMapping("/random")
    fun getRandom(): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.getRandom()
        return ApiResponseGenerator.success(response)
    }

    @GetMapping("")
    fun findAll(@ModelAttribute pageRequestDto: PageRequestDto): ApiResponse<ApiResponse.SuccessBody<ResponseDto<List<Document>>>> {
        val pageResponses = documentService.findAll(pageRequestDto)
        return ApiResponseGenerator.success(convertToResponse(pageResponses))
    }

    @GetMapping("title/{title}")
    fun get(@PathVariable title: String): ApiResponse<ApiResponse.SuccessBody<Any>> {
        val response = documentService.get(title)
        return ApiResponseGenerator.success(response)
    }

    @GetMapping("title/{title}/uuid")
    fun getUuidByTitle(@PathVariable title: String): ApiResponse<ApiResponse.SuccessBody<Any>> {
        val response = documentService.getUuidByTitle(title)
        return ApiResponseGenerator.success(response)
    }

    @GetMapping("uuid/{uuidText}")
    fun getByUuid(@PathVariable uuidText: String): ApiResponse<ApiResponse.SuccessBody<Any>> {
        val uuid = UUID.fromString(uuidText)
        val response = documentService.getByUuid(uuid)
        return ApiResponseGenerator.success(response)
    }

    @GetMapping("uuid/{uuidText}/log")
    fun getLogs(
        @PathVariable uuidText: String,
        @ModelAttribute pageRequestDto: PageRequestDto
    ): ApiResponse<ApiResponse.SuccessBody<ResponseDto<List<LogResponse>>>> {
        val uuid = UUID.fromString(uuidText)
        val pageResponses = logService.findAllByDocumentUuid(uuid, pageRequestDto)
        return ApiResponseGenerator.success(convertToResponse(pageResponses))
    }

    @GetMapping("/log/{logId}")
    fun getDocumentLogs(@PathVariable logId: Long): ApiResponse<ApiResponse.SuccessBody<LogDetailResponse>> {
        val logDetail = logService.getLogDetail(logId)
        return ApiResponseGenerator.success(logDetail)
    }

    @PutMapping
    fun put(
        @RequestBody documentUpdateRequest: DocumentUpdateRequest
    ): ApiResponse<ApiResponse.SuccessBody<DocumentResponse>> {
        val response = documentService.put(documentUpdateRequest.uuid, documentUpdateRequest)
        return ApiResponseGenerator.success(response)
    }

    @GetMapping("/search")
    fun search(@RequestParam keyWord: String): ApiResponse<ApiResponse.SuccessBody<List<DocumentSearchResponse>>> {
        return ApiResponseGenerator.success(documentSearchService.search(keyWord))
    }

    private fun <T> convertToResponse(pageResponses: Page<T>): ResponseDto<List<T>> {
        return ResponseDto.of(
            pageResponses.number,
            pageResponses.totalPages,
            pageResponses.content
        )
    }
}
