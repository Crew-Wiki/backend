package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.graph.dto.CrewGraphElements;

// 크루 그래프의 node와 edge를 어디에서 읽을지 결정하는 전략이다.
// graph.read.source 프로퍼티로 구현체를 고르며, 두 구현체는 같은 응답을 만들어야 한다.
public interface CrewGraphReader {

    CrewGraphElements read(String generation);
}
