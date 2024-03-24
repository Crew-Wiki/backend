package com.wooteco.wiki.service;

import com.wooteco.wiki.dto.DocumentCreateRequest;
import com.wooteco.wiki.dto.DocumentResponse;
import com.wooteco.wiki.dto.DocumentUpdateRequest;
import com.wooteco.wiki.dto.LogFindAllResponse;
import java.util.Optional;

public interface DocumentService {
    DocumentResponse post(DocumentCreateRequest documentCreateRequest);

    Optional<DocumentResponse> get(String title);

    DocumentResponse put(String title, DocumentUpdateRequest documentUpdateRequest);

    LogFindAllResponse getLogs();
}
