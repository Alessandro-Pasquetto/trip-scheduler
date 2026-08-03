package io.github.ale.tripscheduler.service;

import io.github.ale.tripscheduler.dto.ActivityDto;
import io.github.ale.tripscheduler.dto.request.UpdateTripPlanRequest;
import io.github.ale.tripscheduler.dto.response.UserTripPlanDetailResponse;
import io.github.ale.tripscheduler.dto.response.TripPlanSummaryResponse;
import io.github.ale.tripscheduler.entity.Activity;
import io.github.ale.tripscheduler.entity.TripPlan;
import io.github.ale.tripscheduler.entity.TripPlanUser;
import io.github.ale.tripscheduler.entity.UserAccount;
import io.github.ale.tripscheduler.enums.TripRole;
import io.github.ale.tripscheduler.repository.ActivityRepository;
import io.github.ale.tripscheduler.repository.TripPlanRepository;
import io.github.ale.tripscheduler.repository.TripPlanUserRepository;
import io.github.ale.tripscheduler.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripPlanService {

    private final UserAccountRepository userAccountRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanUserRepository tripPlanUserRepository;
    private final ActivityRepository activityRepository;

    public TripPlanService(UserAccountRepository userAccountRepository,
                           TripPlanRepository tripPlanRepository,
                           TripPlanUserRepository tripPlanUserRepository,
                           ActivityRepository activityRepository) {
        this.userAccountRepository = userAccountRepository;
        this.tripPlanRepository = tripPlanRepository;
        this.tripPlanUserRepository = tripPlanUserRepository;
        this.activityRepository = activityRepository;
    }

    public List<TripPlanSummaryResponse> getUserPlans(Long userId) {
        return tripPlanUserRepository.findByUserId(userId)
                .stream()
                .map(TripPlanUser::getTripPlan)
                .map(tripPlan -> TripPlanSummaryResponse.builder()
                        .id(tripPlan.getId())
                        .name(tripPlan.getName())
                        .startDate(tripPlan.getStartDate())
                        .endDate(tripPlan.getEndDate())
                        .build())
                .toList();
    }

    @Transactional
    public Long createPlan(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TripPlan tripPlan = TripPlan.builder()
                .name("New Plan")
                .startDate(null)
                .endDate(null)
                .build();

        TripPlan savedPlan = tripPlanRepository.save(tripPlan);

        TripPlanUser tripPlanUser = TripPlanUser.builder()
                .tripPlan(savedPlan)
                .user(user)
                .role(TripRole.OWNER)
                .build();

        tripPlanUserRepository.save(tripPlanUser);

        return savedPlan.getId();
    }

    public UserTripPlanDetailResponse getUserPlan(Long userId, Long planId) {
        TripPlanUser membership = tripPlanUserRepository.findByTripPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        TripPlan tripPlan = membership.getTripPlan();

        List<Activity> activities = activityRepository.findByTripPlanId(planId);

        UserTripPlanDetailResponse userTripPlanDetailResponse = UserTripPlanDetailResponse.builder()
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
                .tripRole(membership.getRole())
                .build();

        return userTripPlanDetailResponse;
    }

    @Transactional
    public UserTripPlanDetailResponse updateTripPlan(Long userId,
                                                     Long planId,
                                                     UpdateTripPlanRequest tripPlan) {
        TripPlanUser membership = tripPlanUserRepository
                .findByTripPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.VIEWER) {
            throw new RuntimeException("Permission denied");
        }

        TripPlan existingPlan = membership.getTripPlan();

        existingPlan.setName(tripPlan.getName());
        existingPlan.setStartDate(tripPlan.getStartDate());
        existingPlan.setEndDate(tripPlan.getEndDate());

        List<ActivityDto> updatedActivities = updatePlanActivities(existingPlan, tripPlan.getActivities());

        return UserTripPlanDetailResponse.builder()
                .id(existingPlan.getId())
                .name(existingPlan.getName())
                .startDate(existingPlan.getStartDate())
                .endDate(existingPlan.getEndDate())
                .activities(updatedActivities)
                .tripRole(membership.getRole())
                .build();
    }

    private List<ActivityDto> updatePlanActivities(TripPlan tripPlan, List<ActivityDto> activities) {
        List<Activity> existingActivities = activityRepository.findByTripPlanId(tripPlan.getId());

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
                        .tripPlan(tripPlan)
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
        TripPlanUser membership = tripPlanUserRepository.findByTripPlanIdAndUserId(planId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() != TripRole.OWNER)
            throw new RuntimeException("User is not the owner of this TripPlan");

        tripPlanRepository.deleteById(planId);
    }
}
