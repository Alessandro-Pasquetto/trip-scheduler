package io.github.ale.tripscheduler.dto.request;

import io.github.ale.tripscheduler.dto.ActivityDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class UpdateTripPlanRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    private List<ActivityDto> activities;
}
