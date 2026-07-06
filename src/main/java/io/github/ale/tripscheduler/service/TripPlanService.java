package io.github.ale.tripscheduler.service;

import io.github.ale.tripscheduler.dto.ActivityDto;
import io.github.ale.tripscheduler.dto.request.UpdateTripPlanRequest;
import io.github.ale.tripscheduler.dto.response.TripPlanDetailResponse;
import io.github.ale.tripscheduler.dto.response.TripPlanSummaryResponse;
import io.github.ale.tripscheduler.entity.Activity;
import io.github.ale.tripscheduler.entity.TripPlan;
import io.github.ale.tripscheduler.entity.UserAccount;
import io.github.ale.tripscheduler.repository.ActivityRepository;
import io.github.ale.tripscheduler.repository.TripPlanRepository;
import io.github.ale.tripscheduler.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripPlanService {

    private final UserAccountRepository userAccountRepository;
    private final TripPlanRepository tripPlanRepository;
    private final ActivityRepository activityRepository;

    public TripPlanService(UserAccountRepository userAccountRepository,
                           TripPlanRepository tripPlanRepository,
                           ActivityRepository activityRepository) {
        this.userAccountRepository = userAccountRepository;
        this.tripPlanRepository = tripPlanRepository;
        this.activityRepository = activityRepository;
    }

    public List<TripPlanSummaryResponse> getPlans(Long userId) {
        List<TripPlan> tripPlans = tripPlanRepository.findByUserId(userId);

        return tripPlans.stream()
                .map(tripPlan -> TripPlanSummaryResponse.builder()
                        .id(tripPlan.getId())
                        .name(tripPlan.getName())
                        .startDate(tripPlan.getStartDate())
                        .endDate(tripPlan.getEndDate())
                        .build())
                .toList();
    }

    public Long createPlan(Long userId){
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TripPlan tripPlan = TripPlan.builder()
                .user(user)
                .name("New Plan")
                .startDate(null)
                .endDate(null)
                .build();

        TripPlan saved = tripPlanRepository.save(tripPlan);

        return saved.getId();
    }

    public TripPlanDetailResponse getPlan(Long userId, Long planId) {

        TripPlan tripPlan = tripPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        List<Activity> activities = activityRepository.findByTripPlanId(planId);

        TripPlanDetailResponse tripPlanDetailResponse = TripPlanDetailResponse.builder()
                .id(tripPlan.getId())
                .name(tripPlan.getName())
                .startDate(tripPlan.getStartDate())
                .endDate(tripPlan.getEndDate())
                .activities(
                        activities.stream()
                                .map(a -> ActivityDto.builder()
                                        .id(a.getId())
                                        .name(a.getName())
                                        .day(a.getDay())
                                        .startTime(a.getStartTime())
                                        .endTime(a.getEndTime())
                                        .build())
                                .toList()
                )
                .build();

        return tripPlanDetailResponse;
    }

    public TripPlanDetailResponse updateTripPlan(Long userId, Long planId, UpdateTripPlanRequest request) {

        TripPlan existingPlan = tripPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        TripPlan updatedPlan = TripPlan.builder()
                .id(existingPlan.getId())
                .user(existingPlan.getUser())
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        TripPlan savedPlan = tripPlanRepository.save(updatedPlan);

        List<ActivityDto> updatedPlanActivities = updatePlanActivities(savedPlan, request.getActivities());

        TripPlanDetailResponse tripPlanDetailResponse = TripPlanDetailResponse.builder()
                .id(savedPlan.getId())
                .name(savedPlan.getName())
                .startDate(savedPlan.getStartDate())
                .endDate(savedPlan.getEndDate())
                .activities(
                        updatedPlanActivities.stream()
                                .map(a -> ActivityDto.builder()
                                        .id(a.getId())
                                        .name(a.getName())
                                        .day(a.getDay())
                                        .startTime(a.getStartTime())
                                        .endTime(a.getEndTime())
                                        .build())
                                .toList()
                )
                .build();

        return tripPlanDetailResponse;
    }

    private List<ActivityDto> updatePlanActivities(TripPlan savedPlan, List<ActivityDto> requestActivities) {

        List<Activity> existingActivities = activityRepository.findByTripPlanId(savedPlan.getId());

        if (requestActivities == null || requestActivities.isEmpty()) {
            activityRepository.deleteAll(existingActivities);
            return Collections.emptyList();
        }

        Map<Long, Activity> existingMap = existingActivities.stream()
                .collect(Collectors.toMap(Activity::getId, a -> a));

        Set<Long> requestIds = requestActivities.stream()
                .filter(a -> a.getId() != null)
                .map(ActivityDto::getId)
                .collect(Collectors.toSet());

        // DELETE
        List<Activity> toDelete = existingActivities.stream()
                .filter(a -> !requestIds.contains(a.getId()))
                .toList();

        activityRepository.deleteAll(toDelete);

        List<Activity> toSave = new ArrayList<>();

        for (ActivityDto dto : requestActivities) {
            if (dto.getId() == null) {
                Activity newActivity = Activity.builder()
                        .tripPlan(savedPlan)
                        .name(dto.getName())
                        .day(dto.getDay())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .build();

                toSave.add(newActivity);
            } else {
                Activity existing = existingMap.get(dto.getId());

                if (existing != null) {
                    existing.setName(dto.getName());
                    existing.setDay(dto.getDay());
                    existing.setStartTime(dto.getStartTime());
                    existing.setEndTime(dto.getEndTime());

                    toSave.add(existing);
                }
            }
        }

        List<Activity> saved = activityRepository.saveAll(toSave);

        return saved.stream()
                .map(a -> ActivityDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .day(a.getDay())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .build())
                .toList();
    }
}
