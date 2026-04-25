package com.smart.scheduling.service;

import com.smart.scheduling.model.*;
import com.smart.scheduling.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private SchedulingRuleService ruleService;

    @Autowired
    private EmployeeUnavailableDateService unavailableDateService;

    @Autowired
    private OperationLogService operationLogService;

    public ScheduleRecord createScheduleRecord(ScheduleRecord record) {
        record.setId(dataStore.generateScheduleId());
        dataStore.getScheduleMap().put(record.getId(), record);
        return record;
    }

    public List<ScheduleRecord> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<ScheduleRecord> records = new ArrayList<>();
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if ((record.getDate().isEqual(startDate) || record.getDate().isAfter(startDate)) &&
                    (record.getDate().isEqual(endDate) || record.getDate().isBefore(endDate))) {
                records.add(record);
            }
        }
        return records;
    }

    public void deleteSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        Iterator<Map.Entry<Long, ScheduleRecord>> iterator = dataStore.getScheduleMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, ScheduleRecord> entry = iterator.next();
            if ((entry.getValue().getDate().isEqual(startDate) || entry.getValue().getDate().isAfter(startDate)) &&
                    (entry.getValue().getDate().isEqual(endDate) || entry.getValue().getDate().isBefore(endDate))) {
                iterator.remove();
            }
        }
    }

    public List<GapRecord> generateSchedule(LocalDate startDate, LocalDate endDate) {
        deleteSchedulesByDateRange(startDate, endDate);
        clearGapRecordsByDateRange(startDate, endDate);

        List<GapRecord> allGapRecords = new ArrayList<>();
        List<Employee> activeEmployees = employeeService.getActiveEmployees();
        Map<String, List<Employee>> employeesByPosition = groupEmployeesByPosition(activeEmployees);
        Map<Long, Integer> employeeWeeklyShifts = initializeWeeklyShiftCounter(activeEmployees);
        Map<Long, LocalDate> employeeLastNightShift = new HashMap<>();

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            int dayValue = dayOfWeek.getValue();

            if (dayOfWeek == DayOfWeek.MONDAY) {
                resetWeeklyShiftCounter(employeeWeeklyShifts, activeEmployees);
            }

            List<SchedulingRule> rules = ruleService.getRulesByDayOfWeek(dayValue);
            for (SchedulingRule rule : rules) {
                Shift shift = shiftService.getShiftById(rule.getShiftId());
                if (shift == null) continue;

                int requiredCount = rule.getRequiredCount();
                List<Employee> availableEmployees = findAvailableEmployees(
                        employeesByPosition,
                        currentDate,
                        shift,
                        employeeWeeklyShifts,
                        employeeLastNightShift
                );

                int assignedCount = Math.min(requiredCount, availableEmployees.size());
                for (int j = 0; j < assignedCount; j++) {
                    Employee employee = availableEmployees.get(j);
                    ScheduleRecord record = ScheduleRecord.builder()
                            .id(dataStore.generateScheduleId())
                            .employeeId(employee.getId())
                            .shiftId(shift.getId())
                            .date(currentDate)
                            .position(employee.getPosition())
                            .status(1)
                            .build();
                    dataStore.getScheduleMap().put(record.getId(), record);

                    employeeWeeklyShifts.put(employee.getId(), employeeWeeklyShifts.get(employee.getId()) + 1);
                    if (shift.getName().equals("夜班")) {
                        employeeLastNightShift.put(employee.getId(), currentDate);
                    }
                }

                if (assignedCount < requiredCount) {
                    GapRecord gapRecord = GapRecord.builder()
                            .id(dataStore.generateGapId())
                            .date(currentDate)
                            .shiftId(shift.getId())
                            .shiftName(shift.getName())
                            .requiredCount(requiredCount)
                            .actualCount(assignedCount)
                            .gapCount(requiredCount - assignedCount)
                            .build();
                    dataStore.getGapMap().put(gapRecord.getId(), gapRecord);
                    allGapRecords.add(gapRecord);
                }
            }
        }

        operationLogService.addLog(
                "自动排班",
                "系统",
                "从 " + startDate + " 到 " + endDate + " 自动排班完成"
        );

        return allGapRecords;
    }

    private Map<String, List<Employee>> groupEmployeesByPosition(List<Employee> employees) {
        Map<String, List<Employee>> result = new HashMap<>();
        for (Employee employee : employees) {
            result.computeIfAbsent(employee.getPosition(), k -> new ArrayList<>()).add(employee);
        }
        return result;
    }

    private Map<Long, Integer> initializeWeeklyShiftCounter(List<Employee> employees) {
        Map<Long, Integer> counter = new HashMap<>();
        for (Employee employee : employees) {
            counter.put(employee.getId(), 0);
        }
        return counter;
    }

    private void resetWeeklyShiftCounter(Map<Long, Integer> counter, List<Employee> employees) {
        for (Employee employee : employees) {
            counter.put(employee.getId(), 0);
        }
    }

    private List<Employee> findAvailableEmployees(
            Map<String, List<Employee>> employeesByPosition,
            LocalDate currentDate,
            Shift shift,
            Map<Long, Integer> employeeWeeklyShifts,
            Map<Long, LocalDate> employeeLastNightShift
    ) {
        List<Employee> allAvailable = new ArrayList<>();

        for (Map.Entry<String, List<Employee>> entry : employeesByPosition.entrySet()) {
            List<Employee> positionEmployees = new ArrayList<>(entry.getValue());

            positionEmployees.removeIf(employee -> {
                if (employeeWeeklyShifts.getOrDefault(employee.getId(), 0) >= employee.getMaxShiftsPerWeek()) {
                    return true;
                }

                if (unavailableDateService.isEmployeeUnavailable(employee.getId(), currentDate)) {
                    return true;
                }

                if (hasShiftOnDate(employee.getId(), currentDate)) {
                    return true;
                }

                LocalDate lastNightShift = employeeLastNightShift.get(employee.getId());
                if (lastNightShift != null && lastNightShift.plusDays(1).equals(currentDate)) {
                    if (shift.getName().equals("早班")) {
                        return true;
                    }
                }

                return false;
            });

            Collections.sort(positionEmployees, (e1, e2) -> {
                int shifts1 = employeeWeeklyShifts.getOrDefault(e1.getId(), 0);
                int shifts2 = employeeWeeklyShifts.getOrDefault(e2.getId(), 0);
                return Integer.compare(shifts1, shifts2);
            });

            allAvailable.addAll(positionEmployees);
        }

        return allAvailable;
    }

    public boolean hasShiftOnDate(Long employeeId, LocalDate date) {
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            if (record.getEmployeeId().equals(employeeId) && record.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    private void clearGapRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        Iterator<Map.Entry<Long, GapRecord>> iterator = dataStore.getGapMap().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, GapRecord> entry = iterator.next();
            if ((entry.getValue().getDate().isEqual(startDate) || entry.getValue().getDate().isAfter(startDate)) &&
                    (entry.getValue().getDate().isEqual(endDate) || entry.getValue().getDate().isBefore(endDate))) {
                iterator.remove();
            }
        }
    }

    public List<GapRecord> getAllGapRecords() {
        return calculateAllGaps();
    }

    public List<GapRecord> getGapRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        return calculateGapsByDateRange(startDate, endDate);
    }

    private List<GapRecord> calculateAllGaps() {
        List<GapRecord> allGaps = new ArrayList<>();
        
        Set<LocalDate> allDates = new HashSet<>();
        for (ScheduleRecord record : dataStore.getScheduleMap().values()) {
            allDates.add(record.getDate());
        }
        
        for (SchedulingRule rule : dataStore.getRuleMap().values()) {
            for (LocalDate date : allDates) {
                DayOfWeek dayOfWeek = date.getDayOfWeek();
                if (dayOfWeek.getValue() == rule.getDayOfWeek()) {
                    int actualCount = countAssignedEmployees(date, rule.getShiftId());
                    int requiredCount = rule.getRequiredCount();
                    
                    if (actualCount < requiredCount) {
                        Shift shift = shiftService.getShiftById(rule.getShiftId());
                        GapRecord gap = GapRecord.builder()
                                .id(dataStore.generateGapId())
                                .date(date)
                                .shiftId(rule.getShiftId())
                                .shiftName(shift != null ? shift.getName() : "未知")
                                .requiredCount(requiredCount)
                                .actualCount(actualCount)
                                .gapCount(requiredCount - actualCount)
                                .build();
                        allGaps.add(gap);
                    }
                }
            }
        }
        
        allGaps.sort(Comparator.comparing(GapRecord::getDate).thenComparing(GapRecord::getShiftId));
        
        return allGaps;
    }

    private List<GapRecord> calculateGapsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<GapRecord> gaps = new ArrayList<>();
        
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        for (int i = 0; i <= daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            int dayValue = dayOfWeek.getValue();
            
            List<SchedulingRule> rules = ruleService.getRulesByDayOfWeek(dayValue);
            for (SchedulingRule rule : rules) {
                int actualCount = countAssignedEmployees(currentDate, rule.getShiftId());
                int requiredCount = rule.getRequiredCount();
                
                if (actualCount < requiredCount) {
                    Shift shift = shiftService.getShiftById(rule.getShiftId());
                    GapRecord gap = GapRecord.builder()
                            .id(dataStore.generateGapId())
                            .date(currentDate)
                            .shiftId(rule.getShiftId())
                            .shiftName(shift != null ? shift.getName() : "未知")
                            .requiredCount(requiredCount)
                            .actualCount(actualCount)
                            .gapCount(requiredCount - actualCount)
                            .build();
                    gaps.add(gap);
                }
            }
        }
        
        return gaps;
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
}
