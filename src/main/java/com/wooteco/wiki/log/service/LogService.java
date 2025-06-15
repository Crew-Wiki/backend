package com.wooteco.wiki.log.service;

import com.wooteco.wiki.document.exception.DocumentNotFoundException;
import com.wooteco.wiki.log.domain.Log1;
import com.wooteco.wiki.log.domain.dto.LogDetailResponse;
import com.wooteco.wiki.log.domain.dto.LogResponse;
import com.wooteco.wiki.log.repository.LogRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public LogDetailResponse getLogDetail(Long logId) {
        Log1 log = logRepository.findById(logId)
                .orElseThrow(() -> new DocumentNotFoundException("해당 로그가 존재하지 않습니다."));
        return new LogDetailResponse(logId, log.getUuid(), log.getTitle(), log.getContents(), log.getWriter(), log.getGenerateTime());
    }

    public List<LogResponse> getLogs(UUID uuid) {
        List<Log1> logs = logRepository.findAllByUuidOrderByIdAsc(uuid);
        return IntStream.range(0, logs.size())
                .mapToObj(i -> LogResponse.of(logs.get(i), (long) (i + 1)))
                .collect(Collectors.toList());
    }
}
