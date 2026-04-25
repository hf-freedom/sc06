package com.smart.scheduling.controller;

import com.smart.scheduling.model.Result;
import com.smart.scheduling.service.ShiftSwapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/swap")
public class ShiftSwapController {

    @Autowired
    private ShiftSwapService shiftSwapService;

    @PostMapping("/validate")
    public Result<Map<String, Object>> validateSwap(
            @RequestParam Long employee1Id,
            @RequestParam Long employee2Id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        ShiftSwapService.SwapValidationResult result = shiftSwapService.validateSwap(
                employee1Id, employee2Id, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.isValid());
        response.put("errors", result.getErrors());
        
        return Result.success(response);
    }

    @PostMapping("/execute")
    public Result<String> executeSwap(
            @RequestParam Long employee1Id,
            @RequestParam Long employee2Id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        boolean success = shiftSwapService.swapShifts(employee1Id, employee2Id, date);
        if (!success) {
            return Result.error("换班失败，请检查冲突");
        }
        return Result.success("换班成功");
    }

    @PostMapping("/assign")
    public Result<String> assignShift(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long shiftId) {
        
        boolean success = shiftSwapService.assignShift(employeeId, date, shiftId);
        if (!success) {
            return Result.error("排班失败，请检查冲突");
        }
        return Result.success("排班成功");
    }

    @PostMapping("/validate-assign")
    public Result<Map<String, Object>> validateAssignment(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long shiftId) {
        
        ShiftSwapService.SwapValidationResult result = shiftSwapService.validateAssignment(
                employeeId, date, shiftId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.isValid());
        response.put("errors", result.getErrors());
        
        return Result.success(response);
    }

    @DeleteMapping("/remove")
    public Result<String> deleteSchedule(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        boolean success = shiftSwapService.deleteSchedule(employeeId, date);
        if (!success) {
            return Result.error("删除失败，未找到该排班记录");
        }
        return Result.success("删除成功");
    }
}
