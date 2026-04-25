package com.smart.scheduling.service;

import com.smart.scheduling.model.Shift;
import com.smart.scheduling.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShiftService {

    @Autowired
    private DataStore dataStore;

    public List<Shift> getAllShifts() {
        return new ArrayList<>(dataStore.getShiftMap().values());
    }

    public Shift getShiftById(Long id) {
        return dataStore.getShiftMap().get(id);
    }

    public Shift getShiftByName(String name) {
        for (Shift shift : dataStore.getShiftMap().values()) {
            if (shift.getName().equals(name)) {
                return shift;
            }
        }
        return null;
    }

    public Shift createShift(Shift shift) {
        shift.setId(dataStore.generateShiftId());
        dataStore.getShiftMap().put(shift.getId(), shift);
        return shift;
    }

    public Shift updateShift(Long id, Shift shift) {
        Shift existing = dataStore.getShiftMap().get(id);
        if (existing == null) {
            return null;
        }
        shift.setId(id);
        dataStore.getShiftMap().put(id, shift);
        return shift;
    }

    public boolean deleteShift(Long id) {
        return dataStore.getShiftMap().remove(id) != null;
    }
}
