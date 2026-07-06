package io.github.ale.tripscheduler.dto.response;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlanSummaryResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}