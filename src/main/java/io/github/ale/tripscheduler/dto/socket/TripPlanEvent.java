package io.github.ale.tripscheduler.dto.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripPlanEvent {
    private String type;
}