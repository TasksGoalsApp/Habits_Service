package com.example.Habits.repository;

import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "habits")
public class HabitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "habit_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private HabitFrequency habitFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private HabitCategory habitCategory;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private int currentStreak=0;

    @Column(name = "best_streak", nullable = false)
    @Builder.Default
    private int bestStreak=0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active= true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
