package com.smart.scheduling.controller;

import com.smart.scheduling.model.Result;
import com.smart.scheduling.model.SchedulingRule;
import com.smart.scheduling.service.SchedulingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class SchedulingRuleController {

    @Autowired
    private SchedulingRuleService ruleService;

    @GetMapping
    public Result<List<SchedulingRule>> getAllRules() {
        List<SchedulingRule> rules = ruleService.getAllRules();
        return Result.success(rules);
    }

    @GetMapping("/{id}")
    public Result<SchedulingRule> getRuleById(@PathVariable Long id) {
        SchedulingRule rule = ruleService.getRuleById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success(rule);
    }

    @PostMapping
    public Result<SchedulingRule> createRule(@RequestBody SchedulingRule rule) {
        SchedulingRule existing = ruleService.getRuleByDayAndShift(rule.getDayOfWeek(), rule.getShiftId());
        if (existing != null) {
            return Result.error("该星期该班次的规则已存在");
        }
        SchedulingRule created = ruleService.createRule(rule);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<SchedulingRule> updateRule(@PathVariable Long id, @RequestBody SchedulingRule rule) {
        SchedulingRule updated = ruleService.updateRule(id, rule);
        if (updated == null) {
            return Result.error("规则不存在");
        }
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        boolean deleted = ruleService.deleteRule(id);
        if (!deleted) {
            return Result.error("规则不存在");
        }
        return Result.success();
    }
}
