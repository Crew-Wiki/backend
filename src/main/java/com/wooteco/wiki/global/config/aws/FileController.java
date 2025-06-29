package com.wooteco.wiki.global.config.aws;

import com.wooteco.wiki.global.common.ApiResponse;
import com.wooteco.wiki.global.common.ApiResponseGenerator;
import com.wooteco.wiki.global.config.aws.dto.PresignedUrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FileController {
    private final AwsS3Uploader awsS3Uploader;

    @GetMapping("/upload/**")
    public ApiResponse<ApiResponse.SuccessBody<PresignedUrlResponse>> upload(HttpServletRequest request)
            throws IOException {
        String fileKey = request.getRequestURI().substring("/upload/".length());
        return ApiResponseGenerator.success(new PresignedUrlResponse(awsS3Uploader.createPresignedUrl(fileKey)));
    }
}

