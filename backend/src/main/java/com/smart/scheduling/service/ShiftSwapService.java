package com.smart.scheduling.service;

import com.smart.scheduling.model.*;
import com.smart.scheduling.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShiftSwapService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private EmployeeUnavailableDateService unavailableDateService;

    @Autowired
    private SchedulingRuleService ruleService;

    public static class SwapValidationResult {
        private boolean valid;
        private List<String> errors;

        public SwapValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    public SwapValidationResult validateSwap(Long employee1Id, Long employee2Id, LocalDate date) {
        List<String> errors = new ArrayList<>();

        Employee employee1 = employeeService.getEmployeeById(employee1Id);
        Employee employee2 = employeeService.getEmployeeById(employee2Id);

        if (employee1 == null) {
            errors.add("员工1不存在");
        }
        if (employee2 == null) {
            errors.add("员工2不存在");
        }

        if (!errors.isEmpty()) {
            return new SwapValidationResult(false, errors);
        }

        ScheduleRecord record1 = findScheduleRecord(employee1Id, date);
        ScheduleRecord record2 = findScheduleRecord(employee2Id, date);

        if (record1 == null) {
            errors.add("员工1在指定日期没有排班");
        }
        if (record2 == null) {
            errors.add("员工2在指定日期没有排班");
        }

        if (!errors.isEmpty()) {
            return new SwapValidationResult(false, errors);
        }

        if (unavailableDateService.isEmployeeUnavailable(employee1Id, date)) {
            errors.add("员工1在指定日期不可排班");
        }
        if (unavailableDateService.isEmployeeUnavailable(employee2Id, date)) {
            errors.add("员工2在指定日期不可排班");
        }

        if (!errors.isEmpty()) {
            return new SwapValidationResult(false, errors);
        }

        if (checkDayConflict(employee2Id, date, record1.getShiftId())) {
            errors.add("员工2在同一天已有该班次");
        }
        if (checkDayConflict(employee1Id, date, record2.getShiftId())) {
            errors.add("员工1在同一天已有该班次");
        }

        if (!errors.isEmpty()) {
            return new SwapValidationResult(false, errors);
        }

        Shift shift1 = shiftService.getShiftById(record1.getShiftId());
        Shift shift2 = shiftService.getShiftById(record2.getShiftId());

        if (shift2 != null && shift2.getName().equals("夜班")) {
            LocalDate nextDay = date.plusDays(1);
            if (hasMorningShiftAfterNightShift(employee1Id, nextDay)) {
                errors.add("员工1在夜班后的第二天已有早班，无法换班");
            }
        }

        if (shift1 != null && shift1.getName().equals("夜班")) {
            LocalDate nextDay = date.plusDays(1);
            if (hasMorningShiftAfterNightShift(employee2Id, nextDay)) {
                errors.add("员工2在夜班后的第二天已有早班，无法换班");
            }
        }

        if (hasNightShiftPreviousDay(employee1Id, date) && shift1.getName().equals("早班")) {
            errors.add("员工1在前一天有夜班，无法换早班");
        }
        if (hasNightShiftPreviousDay(employee2Id, date) && shift2.getName().equals("早班")) {
            errors.add("员工2在前一天有夜班，无法换早班");
        }

        return errors.isEmpty() 
            ? new SwapValidationResult(true, new ArrayList<>()) 
            : new SwapValidationResult(false, errors);
    }

    public boolean swapShifts(Long employee1Id, Long employee2Id, LocalDate date) {
        SwapValidationResult validation = validateSwap(employee1Id, employee2Id, date);
        if (!validation.isValid()) {
            return false;
        }

        ScheduleRecord record1 = findScheduleRecord(employee1Id, date);
        ScheduleRecord record2 = findScheduleRecord(employee2Id, date);

        if (record1 == null || record2 == null) {
            return false;
        }

        Long tempShiftId = record1.getShiftId();
        record1.setShiftId(record2.getShiftId());
        record2.setShiftId(tempShiftId);

        dataStore.getScheduleMap().put(record1.getId(), record1);
        dataStore.getScheduleMap().put(record2.getId(), record2);

        operationLogService.addLog(
                "手动换班",
                "管理员",
                "员工 " + employee1Id + " 和员工 " + employee2Id + " 在 " + date + " 换班"
        );

        return true;
    }

    public SwapValidationResult validateAssignment(Long employeeId, LocalDate date, Long shiftId) {
        List<String> errors = new ArrayList<>();

        Employee employee = employeeService.getEmployeeById(employeeId);
        Shift shift = shiftService.getShiftById(shiftId);

        if (employee == null) {
            errors.add("员工不存在");
        }
        if (shift == null) {
            errors.add("班次不存在");
        }

        if (!errors.isEmpty()) {
            return new SwapValidationResult(false, errors);
        }

        if (unavailableDateService.isEmployeeUnavailable(employeeId, date)) {
            errors.add("员工在指定日期不可排班");
        }

        if (checkDayConflict(employeeId, date, shiftId)) {
            errors.add("员工在同一天已有班次");
        }

        if (shift.getName().equals("早班")) {
            if (hasNightShiftPreviousDay(employeeId, date)) {
                errors.add("员工在前一天有夜班，第二天不能安排早班");
            }
        }

        int dayOfWeek = date.getDayOfWeek().getValue();
        SchedulingRule rule = ruleService.getRuleByDayAndShift(dayOfWeek, shiftId);
        if (rule != null) {
            int requiredCount = rule.getRequiredCount();
            int currentCount = countAssignedEmployees(date, shiftId);
            if (currentCount >= requiredCount) {
                errors.add("该班次已达到需求人数 (" + currentCount + "/" + requiredCount + ")，无法再安排");
            }
        }

        return errors.isEmpty() 
            ? new SwapValidationResult(true, new ArrayList<>()) 
            : new SwapValidationResult(false, errors);
    }

    private int countAssignedEmployees(LocalDate date, Long shiftId) {
        int count = 0;
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if (record.getDate().equals(date) && record.getShiftId().equals(shiftId)) {
                count++;
            }
        }
        return count;
    }

    public boolean assignShift(Long employeeId, LocalDate date, Long shiftId) {
        SwapValidationResult validation = validateAssignment(employeeId, date, shiftId);
        if (!validation.isValid()) {
            return false;
        }

        Employee employee = employeeService.getEmployeeById(employeeId);

        ScheduleRecord record = ScheduleRecord.builder()
                .id(dataStore.generateScheduleId())
                .employeeId(employeeId)
                .shiftId(shiftId)
                .date(date)
                .position(employee.getPosition())
                .status(1)
                .build();

        dataStore.getScheduleMap().put(record.getId(), record);

        operationLogService.addLog(
                "手动排班",
                "管理员",
                "为员工 " + employeeId + " 在 " + date + " 安排班次 " + shiftId
        );

        return true;
    }

    private boolean checkDayConflict(Long employeeId, LocalDate date, Long excludeShiftId) {
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if (record.getEmployeeId().equals(employeeId) && 
                record.getDate().equals(date) &&
                !record.getShiftId().equals(excludeShiftId)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDayConflict(Long employeeId, LocalDate date) {
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if (record.getEmployeeId().equals(employeeId) && record.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNightShiftPreviousDay(Long employeeId, LocalDate date) {
        LocalDate previousDay = date.minusDays(1);
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if (record.getEmployeeId().equals(employeeId) && record.getDate().equals(previousDay)) {
                Shift shift = shiftService.getShiftById(record.getShiftId());
                if (shift != null && shift.getName().equals("夜班")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasMorningShiftAfterNightShift(Long employeeId, LocalDate date) {
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if (record.getEmployeeId().equals(employeeId) && record.getDate().equals(date)) {
                Shift shift = shiftService.getShiftById(record.getShiftId());
                if (shift != null && shift.getName().equals("早班")) {
                    return true;
                }
            }
        }
        return false;
    }

    private ScheduleRecord findScheduleRecord(Long employeeId, LocalDate date) {
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if (record.getEmployeeId().equals(employeeId) && record.getDate().equals(date)) {
                return record;
            }
        }
        return null;
    }

    public boolean deleteSchedule(Long employeeId, LocalDate date) {
        ScheduleRecord record = findScheduleRecord(employeeId, date);
        if (record == null) {
            return false;
        }

        dataStore.getScheduleMap().remove(record.getId());

        operationLogService.addLog(
                "删除排班",
                "管理员",
                "删除员工 " + employeeId + " 在 " + date + " 的排班"
        );

        return true;
    }
}
