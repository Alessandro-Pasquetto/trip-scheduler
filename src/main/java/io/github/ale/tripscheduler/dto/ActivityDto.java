package io.github.ale.tripscheduler.dto;

import io.github.ale.tripscheduler.enums.ActivityCategory;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityDto {
    private Long id;
    private LocalDate day;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private ActivityCategory category;
}