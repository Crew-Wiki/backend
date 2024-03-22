package com.wooteco.wiki.service;

import com.wooteco.wiki.dto.DocumentCreateRequest;
import com.wooteco.wiki.dto.DocumentResponse;
import com.wooteco.wiki.dto.DocumentUpdateRequest;
import java.util.Optional;

public interface DocumentService {
    DocumentResponse post(String title, DocumentCreateRequest documentCreateRequest);

    Optional<DocumentResponse> get(String title);

    DocumentResponse put(String title, DocumentUpdateRequest documentUpdateRequest);
}
