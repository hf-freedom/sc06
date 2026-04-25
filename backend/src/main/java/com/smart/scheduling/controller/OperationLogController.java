package com.smart.scheduling.controller;

import com.smart.scheduling.model.OperationLog;
import com.smart.scheduling.model.Result;
import com.smart.scheduling.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class OperationLogController {

    @Autowired
    private OperationLogService logService;

    @GetMapping
    public Result<List<OperationLog>> getAllLogs() {
        List<OperationLog> logs = logService.getAllLogs();
        return Result.success(logs);
    }

    @GetMapping("/type/{operationType}")
    public Result<List<OperationLog>> getLogsByType(@PathVariable String operationType) {
        List<OperationLog> logs = logService.getLogsByType(operationType);
        return Result.success(logs);
    }
}
