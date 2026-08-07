package io.github.ale.tripscheduler.dto;

import io.github.ale.tripscheduler.enums.TripRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CollaboratorDto {
    private Long id;
    private String username;
    private TripRole tripRole;
}