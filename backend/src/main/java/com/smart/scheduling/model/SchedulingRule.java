package com.smart.scheduling.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingRule implements Serializable {
    private Long id;
    private Integer dayOfWeek;
    private Long shiftId;
    private Integer requiredCount;
}
