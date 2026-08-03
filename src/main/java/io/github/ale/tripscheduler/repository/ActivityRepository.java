package io.github.ale.tripscheduler.repository;

import io.github.ale.tripscheduler.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>{

    List<Activity> findByTripPlanId(Long tripPlanId);
}
