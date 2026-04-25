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
public class Shift implements Serializable {
    private Long id;
    private String name;
    private String startTime;
    private String endTime;
}
