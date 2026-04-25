package com.smart.scheduling.service;

import com.smart.scheduling.model.Employee;
import com.smart.scheduling.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    @Autowired
    private DataStore dataStore;

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(dataStore.getEmployeeMap().values());
    }

    public Employee getEmployeeById(Long id) {
        return dataStore.getEmployeeMap().get(id);
    }

    public Employee createEmployee(Employee employee) {
        employee.setId(dataStore.generateEmployeeId());
        dataStore.getEmployeeMap().put(employee.getId(), employee);
        return employee;
    }

    public Employee updateEmployee(Long id, Employee employee) {
        Employee existing = dataStore.getEmployeeMap().get(id);
        if (existing == null) {
            return null;
        }
        employee.setId(id);
        dataStore.getEmployeeMap().put(id, employee);
        return employee;
    }

    public boolean deleteEmployee(Long id) {
        return dataStore.getEmployeeMap().remove(id) != null;
    }

    public List<Employee> getActiveEmployees() {
        List<Employee> activeEmployees = new ArrayList<>();
        for (Employee employee : dataStore.getEmployeeMap().values()) {
            if (employee.getStatus() == 1) {
                activeEmployees.add(employee);
            }
        }
        return activeEmployees;
    }
}
