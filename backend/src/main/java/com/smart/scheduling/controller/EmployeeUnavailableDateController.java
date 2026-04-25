package com.smart.scheduling.controller;

import com.smart.scheduling.model.EmployeeUnavailableDate;
import com.smart.scheduling.model.Result;
import com.smart.scheduling.service.EmployeeUnavailableDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unavailable-dates")
public class EmployeeUnavailableDateController {

    @Autowired
    private EmployeeUnavailableDateService unavailableDateService;

    @GetMapping
    public Result<List<EmployeeUnavailableDate>> getAllUnavailableDates() {
        List<EmployeeUnavailableDate> dates = unavailableDateService.getAllUnavailableDates();
        return Result.success(dates);
    }

    @GetMapping("/employee/{employeeId}")
    public Result<List<EmployeeUnavailableDate>> getByEmployeeId(@PathVariable Long employeeId) {
        List<EmployeeUnavailableDate> dates = unavailableDateService.getUnavailableDatesByEmployeeId(employeeId);
        return Result.success(dates);
    }

    @PostMapping
    public Result<EmployeeUnavailableDate> createUnavailableDate(@RequestBody EmployeeUnavailableDate unavailableDate) {
        if (unavailableDateService.isEmployeeUnavailable(
                unavailableDate.getEmployeeId(), 
                unavailableDate.getDate())) {
            return Result.error("该员工在该日期已设置不可排班");
        }
        EmployeeUnavailableDate created = unavailableDateService.createUnavailableDate(unavailableDate);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<EmployeeUnavailableDate> updateUnavailableDate(
            @PathVariable Long id, 
            @RequestBody EmployeeUnavailableDate unavailableDate) {
        EmployeeUnavailableDate updated = unavailableDateService.updateUnavailableDate(id, unavailableDate);
        if (updated == null) {
            return Result.error("记录不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUnavailableDate(@PathVariable Long id) {
        boolean deleted = unavailableDateService.deleteUnavailableDate(id);
        if (!deleted) {
            return Result.error("记录不存在");
        }
        return Result.success();
    }
}
