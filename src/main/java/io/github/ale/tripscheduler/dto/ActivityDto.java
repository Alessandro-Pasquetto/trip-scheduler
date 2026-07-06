package io.github.ale.tripscheduler.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityDto {
    private Long id;
    private String name;
    private LocalDate day;
    private LocalTime startTime;
    private LocalTime endTime;
}