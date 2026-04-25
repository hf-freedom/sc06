package com.smart.scheduling.service;

import com.smart.scheduling.model.EmployeeUnavailableDate;
import com.smart.scheduling.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeUnavailableDateService {

    @Autowired
    private DataStore dataStore;

    public List<EmployeeUnavailableDate> getAllUnavailableDates() {
        return new ArrayList<>(dataStore.getUnavailableDateMap().values());
    }

    public EmployeeUnavailableDate getUnavailableDateById(Long id) {
        return dataStore.getUnavailableDateMap().get(id);
    }

    public List<EmployeeUnavailableDate> getUnavailableDatesByEmployeeId(Long employeeId) {
        List<EmployeeUnavailableDate> dates = new ArrayList<>();
        for (EmployeeUnavailableDate date : dataStore.getUnavailableDateMap().values()) {
            if (date.getEmployeeId().equals(employeeId)) {
                dates.add(date);
            }
        }
        return dates;
    }

    public boolean isEmployeeUnavailable(Long employeeId, LocalDate date) {
        for (EmployeeUnavailableDate unavailableDate : dataStore.getUnavailableDateMap().values()) {
            if (unavailableDate.getEmployeeId().equals(employeeId) && unavailableDate.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    public EmployeeUnavailableDate createUnavailableDate(EmployeeUnavailableDate unavailableDate) {
        unavailableDate.setId(dataStore.generateUnavailableDateId());
        dataStore.getUnavailableDateMap().put(unavailableDate.getId(), unavailableDate);
        return unavailableDate;
    }

    public EmployeeUnavailableDate updateUnavailableDate(Long id, EmployeeUnavailableDate unavailableDate) {
        EmployeeUnavailableDate existing = dataStore.getUnavailableDateMap().get(id);
        if (existing == null) {
            return null;
        }
        unavailableDate.setId(id);
        dataStore.getUnavailableDateMap().put(id, unavailableDate);
        return unavailableDate;
    }

    public boolean deleteUnavailableDate(Long id) {
        return dataStore.getUnavailableDateMap().remove(id) != null;
    }
}
