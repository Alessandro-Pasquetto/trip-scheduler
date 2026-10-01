package io.github.ale.tripscheduler.dto.request;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class UpdateTripPlanDatesRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
