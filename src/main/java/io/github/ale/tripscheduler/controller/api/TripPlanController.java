package io.github.ale.tripscheduler.controller.api;

import io.github.ale.tripscheduler.dto.request.UpdateTripPlanRequest;
import io.github.ale.tripscheduler.dto.response.UserTripPlanDetailResponse;
import io.github.ale.tripscheduler.dto.response.TripPlanSummaryResponse;
import io.github.ale.tripscheduler.security.CustomUserDetails;
import io.github.ale.tripscheduler.service.TripPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trip-plans")
public class TripPlanController {

    private final TripPlanService tripPlanService;

    public TripPlanController(TripPlanService tripPlanService) {
        this.tripPlanService = tripPlanService;
    }

    @GetMapping
    public ResponseEntity<List<TripPlanSummaryResponse>> getPlans(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(tripPlanService.getUserPlans(userDetails.getId()));
    }

    @PostMapping
    public ResponseEntity<Long> createPlan(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(tripPlanService.createPlan(userDetails.getId()));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<UserTripPlanDetailResponse> getPlan(Authentication authentication, @PathVariable Long planId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(tripPlanService.getUserPlan(userDetails.getId(), planId));
    }

    @PutMapping("/{planId}")
    public ResponseEntity<UserTripPlanDetailResponse> updateTripPlan(Authentication authentication,
                                                                     @PathVariable Long planId,
                                                                     @RequestBody UpdateTripPlanRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UserTripPlanDetailResponse response = tripPlanService.updateTripPlan(userDetails.getId(), planId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deleteTripPlan(Authentication authentication,
                                               @PathVariable Long planId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        tripPlanService.deleteTripPlan(userDetails.getId(), planId);

        return ResponseEntity.noContent().build();
    }
}