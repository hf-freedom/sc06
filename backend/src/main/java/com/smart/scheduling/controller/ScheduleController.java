package com.smart.scheduling.controller;

import com.smart.scheduling.model.GapRecord;
import com.smart.scheduling.model.Result;
import com.smart.scheduling.model.ScheduleRecord;
import com.smart.scheduling.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public Result<List<ScheduleRecord>> getSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ScheduleRecord> records = scheduleService.getSchedulesByDateRange(startDate, endDate);
        return Result.success(records);
    }

    @PostMapping("/generate")
    public Result<List<GapRecord>> generateSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return Result.error("开始日期不能晚于结束日期");
        }
        List<GapRecord> gaps = scheduleService.generateSchedule(startDate, endDate);
        return Result.success(gaps);
    }

    @DeleteMapping
    public Result<Void> deleteSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        scheduleService.deleteSchedulesByDateRange(startDate, endDate);
        return Result.success();
    }
}
