package io.github.ale.tripscheduler.dto.response;

import io.github.ale.tripscheduler.dto.ActivityDto;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlanDetailResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<ActivityDto> activities;
}