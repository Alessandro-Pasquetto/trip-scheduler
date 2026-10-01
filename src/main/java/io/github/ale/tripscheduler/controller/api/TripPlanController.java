package io.github.ale.tripscheduler.controller.api;

import io.github.ale.tripscheduler.dto.ActivityDto;
import io.github.ale.tripscheduler.dto.request.CollaborationRequest;
import io.github.ale.tripscheduler.dto.request.UpdateTripPlanDatesRequest;
import io.github.ale.tripscheduler.dto.response.TripPlanDetailResponse;
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

    @GetMapping("/{tripPlanId}")
    public ResponseEntity<TripPlanDetailResponse> getPlan(Authentication authentication,
                                                          @PathVariable Long tripPlanId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(tripPlanService.getUserPlan(userDetails.getId(), tripPlanId));
    }

    @DeleteMapping("/{tripPlanId}")
    public ResponseEntity<Void> deleteTripPlan(Authentication authentication,
                                               @PathVariable Long tripPlanId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        tripPlanService.deleteTripPlan(userDetails.getId(), tripPlanId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{tripPlanId}/dates")
    public ResponseEntity<Void> updateTripPlanDates(Authentication authentication,
                                                    @PathVariable Long tripPlanId,
                                                    @RequestBody UpdateTripPlanDatesRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        tripPlanService.updateTripPlanDates(userDetails.getId(), tripPlanId, request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripPlanId}/activities")
    public ResponseEntity<Long> createTripPlanActivity(Authentication authentication,
                                                       @PathVariable Long tripPlanId,
                                                       @RequestBody ActivityDto request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long activityId = tripPlanService.createTripPlanActivity(userDetails.getId(), tripPlanId, request);

        return ResponseEntity.ok(activityId);
    }

    @PutMapping("/{tripPlanId}/activities/{activityId}")
    public ResponseEntity<Long> updateTripPlanActivity(Authentication authentication,
                                                       @PathVariable Long tripPlanId,
                                                       @PathVariable Long activityId,
                                                       @RequestBody ActivityDto request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        tripPlanService.updateTripPlanActivity(userDetails.getId(), tripPlanId, activityId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tripPlanId}/activities/{activityId}")
    public ResponseEntity<Void> deleteTripPlanActivity(Authentication authentication,
                                                       @PathVariable Long tripPlanId,
                                                       @PathVariable Long activityId) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        tripPlanService.deleteTripPlanActivity(userDetails.getId(), tripPlanId, activityId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripPlanId}/collaboration-requests")
    public ResponseEntity<Void> sendCollaborationRequest(Authentication authentication,
                                                         @PathVariable Long tripPlanId,
                                                         @RequestBody CollaborationRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // todo: temporary until invites are implemented
        tripPlanService.addCollaborator(userDetails.getId(), tripPlanId, request.getUsername());

        return ResponseEntity.noContent().build();
    }
}