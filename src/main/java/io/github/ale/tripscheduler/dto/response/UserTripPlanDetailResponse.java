package io.github.ale.tripscheduler.dto.response;

import io.github.ale.tripscheduler.dto.ActivityDto;
import io.github.ale.tripscheduler.enums.TripRole;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserTripPlanDetailResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<ActivityDto> activities;
    private TripRole tripRole;
}