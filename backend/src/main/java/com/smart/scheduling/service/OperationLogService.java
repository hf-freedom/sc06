package com.smart.scheduling.service;

import com.smart.scheduling.model.OperationLog;
import com.smart.scheduling.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogService {

    @Autowired
    private DataStore dataStore;

    public void addLog(String operationType, String operator, String description) {
        OperationLog log = OperationLog.builder()
                .id(dataStore.generateLogId())
                .operationType(operationType)
                .operator(operator)
                .description(description)
                .operationTime(LocalDateTime.now())
                .build();
        dataStore.getLogMap().put(log.getId(), log);
    }

    public List<OperationLog> getAllLogs() {
        List<OperationLog> logs = new ArrayList<>(dataStore.getLogMap().values());
        logs.sort(Comparator.comparing(OperationLog::getOperationTime).reversed());
        return logs;
    }

    public List<OperationLog> getLogsByType(String operationType) {
        return dataStore.getLogMap().values().stream()
                .filter(log -> log.getOperationType().equals(operationType))
                .sorted(Comparator.comparing(OperationLog::getOperationTime).reversed())
                .collect(Collectors.toList());
    }
}
