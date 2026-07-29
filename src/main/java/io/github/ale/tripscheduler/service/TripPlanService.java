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
import org.springframework.transaction.annotation.Transactional;

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

        TripPlan savedPlan = tripPlanRepository.save(tripPlan);

        return savedPlan.getId();
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
                                .map(activity -> ActivityDto.builder()
                                        .id(activity.getId())
                                        .name(activity.getName())
                                        .day(activity.getDay())
                                        .startTime(activity.getStartTime())
                                        .endTime(activity.getEndTime())
                                        .description(activity.getDescription())
                                        .category(activity.getCategory())
                                        .build())
                                .toList()
                )
                .build();

        return tripPlanDetailResponse;
    }

    public TripPlanDetailResponse updateTripPlan(Long userId, Long planId, UpdateTripPlanRequest tripPlan) {

        TripPlan existingPlan = tripPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        TripPlan updatedPlan = TripPlan.builder()
                .id(existingPlan.getId())
                .user(existingPlan.getUser())
                .name(tripPlan.getName())
                .startDate(tripPlan.getStartDate())
                .endDate(tripPlan.getEndDate())
                .build();

        TripPlan savedPlan = tripPlanRepository.save(updatedPlan);

        List<ActivityDto> updatedPlanActivities = updatePlanActivities(savedPlan, tripPlan.getActivities());

        TripPlanDetailResponse tripPlanDetailResponse = TripPlanDetailResponse.builder()
                .id(savedPlan.getId())
                .name(savedPlan.getName())
                .startDate(savedPlan.getStartDate())
                .endDate(savedPlan.getEndDate())
                .activities(
                        updatedPlanActivities.stream()
                                .map(activity -> ActivityDto.builder()
                                        .id(activity.getId())
                                        .name(activity.getName())
                                        .day(activity.getDay())
                                        .startTime(activity.getStartTime())
                                        .endTime(activity.getEndTime())
                                        .description(activity.getDescription())
                                        .category(activity.getCategory())
                                        .build())
                                .toList()
                )
                .build();

        return tripPlanDetailResponse;
    }

    private List<ActivityDto> updatePlanActivities(TripPlan savedPlan, List<ActivityDto> activities) {

        List<Activity> existingActivities = activityRepository.findByTripPlanId(savedPlan.getId());

        if (activities == null || activities.isEmpty()) {
            activityRepository.deleteAll(existingActivities);
            return Collections.emptyList();
        }

        Map<Long, Activity> existingActivitiesMap = existingActivities.stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        Set<Long> requestIds = activities.stream()
                .filter(activity -> activity.getId() != null)
                .map(ActivityDto::getId)
                .collect(Collectors.toSet());

        // DELETE
        List<Activity> activitiesToDelete = existingActivities.stream()
                .filter(activity -> !requestIds.contains(activity.getId()))
                .toList();

        activityRepository.deleteAll(activitiesToDelete);

        // ADD AND EDIT
        List<Activity> activitiesToSave = new ArrayList<>();

        for (ActivityDto activity : activities) {
            if (activity.getId() == null) {
                Activity newActivity = Activity.builder()
                        .tripPlan(savedPlan)
                        .name(activity.getName())
                        .day(activity.getDay())
                        .startTime(activity.getStartTime())
                        .endTime(activity.getEndTime())
                        .description(activity.getDescription())
                        .category(activity.getCategory())
                        .build();

                activitiesToSave.add(newActivity);
            } else {
                Activity existing = existingActivitiesMap.get(activity.getId());

                if (existing != null) {
                    existing.setName(activity.getName());
                    existing.setDay(activity.getDay());
                    existing.setStartTime(activity.getStartTime());
                    existing.setEndTime(activity.getEndTime());
                    existing.setDescription(activity.getDescription());
                    existing.setCategory(activity.getCategory());

                    activitiesToSave.add(existing);
                }
            }
        }

        List<Activity> savedActivities = activityRepository.saveAll(activitiesToSave);

        return savedActivities.stream()
                .map(activity -> ActivityDto.builder()
                        .id(activity.getId())
                        .name(activity.getName())
                        .day(activity.getDay())
                        .startTime(activity.getStartTime())
                        .endTime(activity.getEndTime())
                        .description(activity.getDescription())
                        .category(activity.getCategory())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteTripPlan(Long userId, Long planId) {

        TripPlan plan = tripPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        activityRepository.deleteByTripPlanId(planId);
        tripPlanRepository.delete(plan);
    }
}
