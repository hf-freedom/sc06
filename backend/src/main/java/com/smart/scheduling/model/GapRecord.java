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
public class GapRecord implements Serializable {
    private Long id;
    private LocalDate date;
    private Long shiftId;
    private String shiftName;
    private Integer requiredCount;
    private Integer actualCount;
    private Integer gapCount;
}
