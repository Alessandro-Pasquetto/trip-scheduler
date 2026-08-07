package io.github.ale.tripscheduler.dto.response;

import io.github.ale.tripscheduler.dto.ActivityDto;
import io.github.ale.tripscheduler.dto.CollaboratorDto;
import io.github.ale.tripscheduler.enums.TripRole;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlanDetailResponse {
    private Long id;
    private String name;
    private TripRole tripRole;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ActivityDto> activities;
    private List<CollaboratorDto> collaborators;
}