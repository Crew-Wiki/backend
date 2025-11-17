package com.wooteco.wiki.admin.service;

import com.wooteco.wiki.document.service.DocumentService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final DocumentService documentService;

    public AdminService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void deleteDocumentByDocumentUuid(UUID documentUuid) {
        documentService.deleteByUuid(documentUuid);
    }
}
