package com.wooteco.wiki.admin.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminService {

    private final CrewDocumentService crewDocumentService;

    public void deleteDocumentByDocumentUuid(UUID documentUuid) {
        crewDocumentService.deleteByUuid(documentUuid);
    }
}
