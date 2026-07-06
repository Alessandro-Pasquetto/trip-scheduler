package io.github.ale.tripscheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "trip_plan")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;
}