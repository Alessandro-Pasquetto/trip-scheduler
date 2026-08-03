package io.github.ale.tripscheduler.entity;

import io.github.ale.tripscheduler.enums.TripRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_plan_user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trip_plan_id", "user_id"})
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlanUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripRole role;
}
