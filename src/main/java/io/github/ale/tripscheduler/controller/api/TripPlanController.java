package io.github.ale.tripscheduler.controller.api;

import io.github.ale.tripscheduler.dto.request.CollaborationRequestDto;
import io.github.ale.tripscheduler.dto.request.UpdateTripPlanRequest;
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

    @PutMapping("/{tripPlanId}")
    public ResponseEntity<Void> updateTripPlan(Authentication authentication,
                                               @PathVariable Long tripPlanId,
                                               @RequestBody UpdateTripPlanRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        tripPlanService.updateTripPlan(userDetails.getId(), tripPlanId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tripPlanId}")
    public ResponseEntity<Void> deleteTripPlan(Authentication authentication,
                                               @PathVariable Long tripPlanId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        tripPlanService.deleteTripPlan(userDetails.getId(), tripPlanId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tripPlanId}/collaboration-requests")
    public ResponseEntity<Void> sendCollaborationRequest(Authentication authentication,
                                                         @PathVariable Long tripPlanId,
                                                         @RequestBody CollaborationRequestDto request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // todo: temporary until invites are implemented
        tripPlanService.addCollaborator(userDetails.getId(), tripPlanId, request.getUsername());

        return ResponseEntity.ok().build();
    }
}