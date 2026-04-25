package com.smart.scheduling.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRecord implements Serializable {
    private Long id;
    private Long employeeId;
    private Long shiftId;
    private LocalDate date;
    private String position;
    private Integer status;
}
