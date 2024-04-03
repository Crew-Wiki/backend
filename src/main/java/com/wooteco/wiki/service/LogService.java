package com.wooteco.wiki.service;

import com.wooteco.wiki.dto.LogDetailResponse;
import com.wooteco.wiki.dto.LogResponse;
import com.wooteco.wiki.entity.Log;
import com.wooteco.wiki.repository.LogRepository;
import java.util.List;
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
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("해당 로그가 존재하지 않습니다."));
        return new LogDetailResponse(logId, log.getTitle(), log.getContents(), log.getWriter(), log.getGenerateTime());
    }

    public List<LogResponse> getLogs(String title) {
        List<Log> logs = logRepository.findAllByTitleOrderByLogIdAsc(title);
        return IntStream.range(0, logs.size())
                .mapToObj(i -> LogResponse.of(logs.get(i), (long) (i + 1)))
                .collect(Collectors.toList());
    }
}
