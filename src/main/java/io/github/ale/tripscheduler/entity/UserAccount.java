package io.github.ale.tripscheduler.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_account")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;
}
