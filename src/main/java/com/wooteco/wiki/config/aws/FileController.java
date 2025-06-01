package com.wooteco.wiki.config.aws;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class FileController {
    private final AwsS3Uploader awsS3Uploader;

    @GetMapping("/upload/{fileKey}")
    public String upload(@PathVariable(value = "fileKey") String fileKey) throws IOException {
        return awsS3Uploader.createPresignedUrl(fileKey).toString();
    }
}

