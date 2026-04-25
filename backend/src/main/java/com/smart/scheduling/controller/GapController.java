package com.smart.scheduling.controller;

import com.smart.scheduling.model.GapRecord;
import com.smart.scheduling.model.Result;
import com.smart.scheduling.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/gaps")
public class GapController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public Result<List<GapRecord>> getAllGaps() {
        List<GapRecord> gaps = scheduleService.getAllGapRecords();
        return Result.success(gaps);
    }

    @GetMapping("/range")
    public Result<List<GapRecord>> getGapsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<GapRecord> gaps = scheduleService.getGapRecordsByDateRange(startDate, endDate);
        return Result.success(gaps);
    }
}
