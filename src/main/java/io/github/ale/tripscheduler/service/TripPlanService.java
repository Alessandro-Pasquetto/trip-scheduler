package io.github.ale.tripscheduler.service;

import io.github.ale.tripscheduler.dto.ActivityDto;
import io.github.ale.tripscheduler.dto.CollaboratorDto;
import io.github.ale.tripscheduler.dto.request.UpdateTripPlanDatesRequest;
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
    public void deleteTripPlan(Long userId, Long tripPlanId) {
        TripPlanUser membership = tripPlanUserRepository.findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.OWNER) {
            List<TripPlanUser> collaborators = tripPlanUserRepository.findByTripPlanId(tripPlanId);

            for (TripPlanUser collaborator : collaborators) {
                messagingTemplate.convertAndSendToUser(
                        collaborator.getUser().getUsername(),
                        "/queue/notifications",
                        new TripPlanEvent("PLAN_DELETED", tripPlanId)
                );
            }

            tripPlanRepository.deleteById(tripPlanId);
        }
        else
            tripPlanUserRepository.deleteByTripPlanIdAndUserId(tripPlanId, userId);
    }

    @Transactional
    public void updateTripPlanDates(Long userId, Long tripPlanId, UpdateTripPlanDatesRequest request) {
        TripPlanUser membership = tripPlanUserRepository
                .findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.VIEWER)
            throw new RuntimeException("Permission denied");

        TripPlan existingPlan = membership.getTripPlan();

        existingPlan.setStartDate(request.getStartDate());
        existingPlan.setEndDate(request.getEndDate());
    }

    @Transactional
    public Long createTripPlanActivity(Long userId, Long tripPlanId, ActivityDto request) {

        TripPlanUser membership = tripPlanUserRepository
                .findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.VIEWER)
            throw new RuntimeException("Permission denied");

        TripPlan tripPlan = membership.getTripPlan();

        Activity activity = Activity.builder()
                .tripPlan(tripPlan)
                .day(request.getDay())
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();

        activityRepository.save(activity);

        request.setId(activity.getId());

        messagingTemplate.convertAndSend(
                "/topic/trip-plan/" + tripPlanId,
                new TripPlanEvent("ACTIVITY_ADDED", request)
        );

        return activity.getId();
    }

    @Transactional
    public void updateTripPlanActivity(Long userId, Long tripPlanId, Long activityId, ActivityDto request) {

        TripPlanUser membership = tripPlanUserRepository
                .findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.VIEWER)
            throw new RuntimeException("Permission denied");

        Activity activity = activityRepository
                .findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!activity.getTripPlan().getId().equals(tripPlanId))
            throw new RuntimeException("Activity does not belong to this trip plan");

        activity.setDay(request.getDay());
        activity.setName(request.getName());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());

        activityRepository.save(activity);

        messagingTemplate.convertAndSend(
                "/topic/trip-plan/" + tripPlanId,
                new TripPlanEvent("ACTIVITY_UPDATED", request)
        );
    }

    @Transactional
    public void deleteTripPlanActivity(Long userId, Long tripPlanId, Long activityId) {

        TripPlanUser membership = tripPlanUserRepository
                .findByTripPlanIdAndUserId(tripPlanId, userId)
                .orElseThrow(() -> new RuntimeException("TripPlan not found"));

        if (membership.getRole() == TripRole.VIEWER)
            throw new RuntimeException("Permission denied");

        Activity activity = activityRepository
                .findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!activity.getTripPlan().getId().equals(tripPlanId))
            throw new RuntimeException("Activity does not belong to this trip plan");

        activityRepository.delete(activity);

        messagingTemplate.convertAndSend(
                "/topic/trip-plan/" + tripPlanId,
                new TripPlanEvent("ACTIVITY_DELETED", activityId)
        );
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

        messagingTemplate.convertAndSendToUser(
                collaboratorUsername,
                "/queue/notifications",
                new TripPlanEvent("PLAN_ADDED", null)
        );
    }
}