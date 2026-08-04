package io.github.ale.tripscheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_plan")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}