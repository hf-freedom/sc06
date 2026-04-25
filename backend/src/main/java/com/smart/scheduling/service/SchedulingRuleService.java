package com.smart.scheduling.service;

import com.smart.scheduling.model.SchedulingRule;
import com.smart.scheduling.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SchedulingRuleService {

    @Autowired
    private DataStore dataStore;

    public List<SchedulingRule> getAllRules() {
        return new ArrayList<>(dataStore.getRuleMap().values());
    }

    public SchedulingRule getRuleById(Long id) {
        return dataStore.getRuleMap().get(id);
    }

    public List<SchedulingRule> getRulesByDayOfWeek(Integer dayOfWeek) {
        List<SchedulingRule> rules = new ArrayList<>();
        for (SchedulingRule rule : dataStore.getRuleMap().values()) {
            if (rule.getDayOfWeek().equals(dayOfWeek)) {
                rules.add(rule);
            }
        }
        return rules;
    }

    public SchedulingRule getRuleByDayAndShift(Integer dayOfWeek, Long shiftId) {
        for (SchedulingRule rule : dataStore.getRuleMap().values()) {
            if (rule.getDayOfWeek().equals(dayOfWeek) && rule.getShiftId().equals(shiftId)) {
                return rule;
            }
        }
        return null;
    }

    public SchedulingRule createRule(SchedulingRule rule) {
        rule.setId(dataStore.generateRuleId());
        dataStore.getRuleMap().put(rule.getId(), rule);
        return rule;
    }

    public SchedulingRule updateRule(Long id, SchedulingRule rule) {
        SchedulingRule existing = dataStore.getRuleMap().get(id);
        if (existing == null) {
            return null;
        }
        rule.setId(id);
        dataStore.getRuleMap().put(id, rule);
        return rule;
    }

    public boolean deleteRule(Long id) {
        return dataStore.getRuleMap().remove(id) != null;
    }
}
