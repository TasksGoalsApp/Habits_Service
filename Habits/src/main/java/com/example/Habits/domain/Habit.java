package com.example.Habits.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class Habit {

    private Long id;
    private Long userId;
    private String name;
    private HabitFrequency habitFrequency;
    private HabitCategory habitCategory;
    @Builder.Default
    private int currentStreak=0;
    @Builder.Default
    private int bestStreak=0;
    @Builder.Default
    private boolean active= true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void incrementStreak() {
        ensureActive();

        this.currentStreak++;

        if (this.currentStreak > this.bestStreak) {
            this.bestStreak = this.currentStreak;
        }
    }
    public void resetCurrentStreak() {
        this.currentStreak = 0;
    }

    public void ensureActive() {
        if (!active) {
            throw new IllegalStateException(
                    "An inactive habit cannot be completed"
            );
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Habit name cannot be blank"
            );
        }

        if (name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Habit name cannot exceed 100 characters"
            );
        }
    }

    private void validateStreak(int currentStreak, int bestStreak) {
        if (currentStreak < 0) {
            throw new IllegalArgumentException(
                    "Current streak cannot be negative"
            );
        }

        if (bestStreak < 0) {
            throw new IllegalArgumentException(
                    "Best streak cannot be negative"
            );
        }

        if (bestStreak < currentStreak) {
            throw new IllegalArgumentException(
                    "Best streak cannot be smaller than current streak"
            );
        }
    }
    private void updateStreak(int newCurrentStreak, int newBestStreak) {
        validateStreak(newCurrentStreak, newBestStreak);

        this.currentStreak = newCurrentStreak;
        this.bestStreak = newBestStreak;
    }
}
