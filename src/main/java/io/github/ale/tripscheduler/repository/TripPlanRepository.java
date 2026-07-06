package io.github.ale.tripscheduler.repository;

import io.github.ale.tripscheduler.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    List<TripPlan> findByUserId(Long userId);

    Optional<TripPlan> findByIdAndUserId(Long id, Long userId);

}