package com.wooteco.wiki.graph.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.hibernate.resource.jdbc.spi.StatementInspector;

public class RecordingStatementInspector implements StatementInspector {

    private static final List<String> STATEMENTS = new CopyOnWriteArrayList<>();

    public static void clear() {
        STATEMENTS.clear();
    }

    public static List<String> statements() {
        return List.copyOf(STATEMENTS);
    }

    @Override
    public String inspect(String sql) {
        STATEMENTS.add(sql);
        return sql;
    }
}
