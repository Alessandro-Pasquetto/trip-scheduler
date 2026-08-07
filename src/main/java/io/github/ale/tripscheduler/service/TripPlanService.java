package io.github.ale.tripscheduler.service;

import io.github.ale.tripscheduler.dto.ActivityDto;
import io.github.ale.tripscheduler.dto.CollaboratorDto;
import io.github.ale.tripscheduler.dto.request.UpdateTripPlanRequest;
import io.github.ale.tripscheduler.dto.response.TripPlanDetailResponse;
import io.github.ale.tripscheduler.dto.response.TripPlanSummaryResponse;
import io.github.ale.tripscheduler.dto.socket.TripPlanEvent;
import io.github.ale.tripscheduler.entity.Activity;
import io.github.ale.tripscheduler.entity.TripPlan;
import io.github.ale.tripscheduler.entity.TripPlanUser;
import io.github.ale.tripscheduler.entity.UserAccount;
import io.github.ale.tripscheduler.enums.TripRole;
import io.github.ale.tripscheduler.repository.ActivityRepository;
import io.github.ale.tripscheduler.repository.TripPlanRepository;
import io.github.ale.tripscheduler.repository.TripPlanUserRepository;
import io.github.ale.tripscheduler.repository.UserAccountRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TripPlanService {

    private final UserAccountRepository userAccountRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanUserRepository tripPlanUserRepository;
    private final ActivityRepository activityRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public TripPlanService(UserAccountRepository userAccountRepository,
                           TripPlanRepository tripPlanRepository,
                           TripPlanUserRepository tripPlanUserRepository,
                           ActivityRepository activityRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.userAccountRepository = userAccountRepository;
        this.tripPlanRepository = tripPlanRepository;
        this.tripPlanUserRepository = tripPlanUserRepository;
        this.activityRepository = activityRepository;
        this.messagingTemplate = messagingTemplate;
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
                        .updatedAt(tripPlan.getUpdatedAt())
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

    public TripPlanDetailResponse getUserPlan(Long userId, Long tripPlanId) {
        TripPlanUser membership = tripPlanUserRepository.findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        TripPlan tripPlan = membership.getTripPlan();

        List<Activity> activities = activityRepository.findByTripPlanId(tripPlanId);

        List<TripPlanUser> collaborators = tripPlanUserRepository.findByTripPlanId(tripPlanId);

        TripPlanDetailResponse tripPlanDetailResponse = TripPlanDetailResponse.builder()
                .id(tripPlan.getId())
                .name(tripPlan.getName())
                .tripRole(membership.getRole())
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
                .collaborators(
                        collaborators.stream()
                                .map(member -> CollaboratorDto.builder()
                                        .id(member.getUser().getId())
                                        .username(member.getUser().getUsername())
                                        .tripRole(member.getRole())
                                        .build())
                                .toList()
                )
                .build();

        return tripPlanDetailResponse;
    }

    @Transactional
    public void updateTripPlan(Long userId, Long tripPlanId, UpdateTripPlanRequest tripPlan) {
        TripPlanUser membership = tripPlanUserRepository
                .findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.VIEWER)
            throw new RuntimeException("Permission denied");

        TripPlan existingPlan = membership.getTripPlan();

        existingPlan.setName(tripPlan.getName());
        existingPlan.setStartDate(tripPlan.getStartDate());
        existingPlan.setEndDate(tripPlan.getEndDate());

        updatePlanActivities(existingPlan, tripPlan.getActivities());

        messagingTemplate.convertAndSend(
                "/topic/trip-plan/" + tripPlanId,
                new TripPlanEvent("UPDATE")
        );
    }

    private void updatePlanActivities(TripPlan tripPlan, List<ActivityDto> activities) {
        List<Activity> existingActivities = activityRepository.findByTripPlanId(tripPlan.getId());

        if (activities == null || activities.isEmpty()) {
            if (!existingActivities.isEmpty()) {
                activityRepository.deleteAll(existingActivities);
                tripPlan.setUpdatedAt(LocalDateTime.now());
            }
            return;
        }

        Map<Long, Activity> existingActivitiesMap = existingActivities.stream()
                .collect(Collectors.toMap(Activity::getId, activity -> activity));

        Set<Long> requestIds = activities.stream()
                .map(ActivityDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        boolean modified = false;

        // DELETE
        List<Activity> activitiesToDelete = existingActivities.stream()
                .filter(activity -> !requestIds.contains(activity.getId()))
                .toList();

        if (!activitiesToDelete.isEmpty()) {
            activityRepository.deleteAll(activitiesToDelete);
            modified = true;
        }

        // ADD + UPDATE
        List<Activity> activitiesToSave = new ArrayList<>();

        for (ActivityDto dto : activities) {
            // ADD
            if (dto.getId() == null) {
                Activity newActivity = Activity.builder()
                        .tripPlan(tripPlan)
                        .name(dto.getName())
                        .day(dto.getDay())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .description(dto.getDescription())
                        .category(dto.getCategory())
                        .build();

                activitiesToSave.add(newActivity);
                modified = true;
                continue;
            }

            // UPDATE
            Activity existing = existingActivitiesMap.get(dto.getId());

            if (existing != null) {
                boolean changed =
                        !Objects.equals(existing.getName(), dto.getName()) ||
                        !Objects.equals(existing.getDay(), dto.getDay()) ||
                        !Objects.equals(existing.getStartTime(), dto.getStartTime()) ||
                        !Objects.equals(existing.getEndTime(), dto.getEndTime()) ||
                        !Objects.equals(existing.getDescription(), dto.getDescription()) ||
                        !Objects.equals(existing.getCategory(), dto.getCategory());

                if (changed) {
                    existing.setName(dto.getName());
                    existing.setDay(dto.getDay());
                    existing.setStartTime(dto.getStartTime());
                    existing.setEndTime(dto.getEndTime());
                    existing.setDescription(dto.getDescription());
                    existing.setCategory(dto.getCategory());

                    activitiesToSave.add(existing);
                    modified = true;
                }
            }
        }

        if (!activitiesToSave.isEmpty())
            activityRepository.saveAll(activitiesToSave);

        if (modified)
            tripPlan.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void deleteTripPlan(Long userId, Long tripPlanId) {
        TripPlanUser membership = tripPlanUserRepository.findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.OWNER)
            tripPlanRepository.deleteById(tripPlanId);
        else
            tripPlanUserRepository.deleteByTripPlanIdAndUserId(tripPlanId, userId);
    }

    // todo: temporary until invites are implemented
    @Transactional
    public void addCollaborator(Long userId, Long tripPlanId, String collaboratorUsername) {
        TripPlanUser ownerMembership = tripPlanUserRepository.findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (ownerMembership.getRole() != TripRole.OWNER)
            throw new RuntimeException("Permission denied");

        UserAccount collaborator = userAccountRepository.findByUsername(collaboratorUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyExists = tripPlanUserRepository.findByTripPlanIdAndUserId(tripPlanId, collaborator.getId())
                .isPresent();

        if (alreadyExists)
            throw new RuntimeException("User already collaborator");

        TripPlanUser newCollaborator = TripPlanUser.builder()
                .tripPlan(ownerMembership.getTripPlan())
                .user(collaborator)
                .role(TripRole.EDITOR)
                .build();

        tripPlanUserRepository.save(newCollaborator);

        messagingTemplate.convertAndSend(
                "/topic/trip-plan/" + tripPlanId,
                new TripPlanEvent("UPDATE")
        );
    }
}