package com.smart.scheduling.controller;

import com.smart.scheduling.model.Result;
import com.smart.scheduling.model.Shift;
import com.smart.scheduling.service.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    @Autowired
    private ShiftService shiftService;

    @GetMapping
    public Result<List<Shift>> getAllShifts() {
        List<Shift> shifts = shiftService.getAllShifts();
        return Result.success(shifts);
    }

    @GetMapping("/{id}")
    public Result<Shift> getShiftById(@PathVariable Long id) {
        Shift shift = shiftService.getShiftById(id);
        if (shift == null) {
            return Result.error("班次不存在");
        }
        return Result.success(shift);
    }

    @PostMapping
    public Result<Shift> createShift(@RequestBody Shift shift) {
        Shift created = shiftService.createShift(shift);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Shift> updateShift(@PathVariable Long id, @RequestBody Shift shift) {
        Shift updated = shiftService.updateShift(id, shift);
        if (updated == null) {
            return Result.error("班次不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteShift(@PathVariable Long id) {
        boolean deleted = shiftService.deleteShift(id);
        if (!deleted) {
            return Result.error("班次不存在");
        }
        return Result.success();
    }
}
