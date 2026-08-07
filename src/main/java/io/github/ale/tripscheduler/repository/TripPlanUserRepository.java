package io.github.ale.tripscheduler.repository;

import io.github.ale.tripscheduler.entity.TripPlanUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripPlanUserRepository extends JpaRepository<TripPlanUser, Long> {

    List<TripPlanUser> findByUserId(Long userId);
    List<TripPlanUser> findByTripPlanId(Long tripPlanId);
    Optional<TripPlanUser> findByTripPlanIdAndUserId(Long tripPlanId, Long userId);
    void deleteByTripPlanIdAndUserId(Long tripPlanId, Long userId);
}
