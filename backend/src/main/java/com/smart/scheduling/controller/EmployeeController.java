package com.smart.scheduling.controller;

import com.smart.scheduling.model.Employee;
import com.smart.scheduling.model.Result;
import com.smart.scheduling.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public Result<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return Result.success(employees);
    }

    @GetMapping("/{id}")
    public Result<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return Result.error("员工不存在");
        }
        return Result.success(employee);
    }

    @PostMapping
    public Result<Employee> createEmployee(@RequestBody Employee employee) {
        Employee created = employeeService.createEmployee(employee);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        Employee updated = employeeService.updateEmployee(id, employee);
        if (updated == null) {
            return Result.error("员工不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteEmployee(@PathVariable Long id) {
        boolean deleted = employeeService.deleteEmployee(id);
        if (!deleted) {
            return Result.error("员工不存在");
        }
        return Result.success();
    }
}
