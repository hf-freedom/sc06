package com.smart.scheduling.store;

import com.smart.scheduling.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class DataStore {

    private AtomicLong employeeIdGenerator = new AtomicLong(1);
    private AtomicLong shiftIdGenerator = new AtomicLong(1);
    private AtomicLong ruleIdGenerator = new AtomicLong(1);
    private AtomicLong unavailableDateIdGenerator = new AtomicLong(1);
    private AtomicLong scheduleIdGenerator = new AtomicLong(1);
    private AtomicLong gapIdGenerator = new AtomicLong(1);
    private AtomicLong logIdGenerator = new AtomicLong(1);

    private ConcurrentHashMap<Long, Employee> employeeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, Shift> shiftMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, SchedulingRule> ruleMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, EmployeeUnavailableDate> unavailableDateMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, ScheduleRecord> scheduleMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, GapRecord> gapMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, OperationLog> logMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        initShifts();
        initEmployees();
        initRules();
    }

    private void initShifts() {
        Shift morningShift = Shift.builder()
                .id(shiftIdGenerator.getAndIncrement())
                .name("早班")
                .startTime("08:00")
                .endTime("16:00")
                .build();
        shiftMap.put(morningShift.getId(), morningShift);

        Shift afternoonShift = Shift.builder()
                .id(shiftIdGenerator.getAndIncrement())
                .name("中班")
                .startTime("12:00")
                .endTime("20:00")
                .build();
        shiftMap.put(afternoonShift.getId(), afternoonShift);

        Shift eveningShift = Shift.builder()
                .id(shiftIdGenerator.getAndIncrement())
                .name("晚班")
                .startTime("16:00")
                .endTime("24:00")
                .build();
        shiftMap.put(eveningShift.getId(), eveningShift);

        Shift nightShift = Shift.builder()
                .id(shiftIdGenerator.getAndIncrement())
                .name("夜班")
                .startTime("00:00")
                .endTime("08:00")
                .build();
        shiftMap.put(nightShift.getId(), nightShift);
    }

    private void initEmployees() {
        for (int i = 1; i <= 5; i++) {
            Employee employee = Employee.builder()
                    .id(employeeIdGenerator.getAndIncrement())
                    .name("员工" + i)
                    .position("客服")
                    .department("客户服务部")
                    .maxShiftsPerWeek(5)
                    .status(1)
                    .build();
            employeeMap.put(employee.getId(), employee);
        }
    }

    private void initRules() {
        List<Long> shiftIds = new ArrayList<>(shiftMap.keySet());
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            for (Long shiftId : shiftIds) {
                SchedulingRule rule = SchedulingRule.builder()
                        .id(ruleIdGenerator.getAndIncrement())
                        .dayOfWeek(dayOfWeek)
                        .shiftId(shiftId)
                        .requiredCount(2)
                        .build();
                ruleMap.put(rule.getId(), rule);
            }
        }
    }

    public long generateEmployeeId() {
        return employeeIdGenerator.getAndIncrement();
    }

    public long generateShiftId() {
        return shiftIdGenerator.getAndIncrement();
    }

    public long generateRuleId() {
        return ruleIdGenerator.getAndIncrement();
    }

    public long generateUnavailableDateId() {
        return unavailableDateIdGenerator.getAndIncrement();
    }

    public long generateScheduleId() {
        return scheduleIdGenerator.getAndIncrement();
    }

    public long generateGapId() {
        return gapIdGenerator.getAndIncrement();
    }

    public long generateLogId() {
        return logIdGenerator.getAndIncrement();
    }

    public ConcurrentHashMap<Long, Employee> getEmployeeMap() {
        return employeeMap;
    }

    public ConcurrentHashMap<Long, Shift> getShiftMap() {
        return shiftMap;
    }

    public ConcurrentHashMap<Long, SchedulingRule> getRuleMap() {
        return ruleMap;
    }

    public ConcurrentHashMap<Long, EmployeeUnavailableDate> getUnavailableDateMap() {
        return unavailableDateMap;
    }

    public ConcurrentHashMap<Long, ScheduleRecord> getScheduleMap() {
        return scheduleMap;
    }

    public ConcurrentHashMap<Long, GapRecord> getGapMap() {
        return gapMap;
    }

    public ConcurrentHashMap<Long, OperationLog> getLogMap() {
        return logMap;
    }
}
