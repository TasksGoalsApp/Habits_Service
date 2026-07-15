package com.example.Habits.domain;

import com.example.Habits.exception.InactiveHabitException;
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
    private int currentStreak=0;
    private int bestStreak=0;
    private boolean active= true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void completeConsecutivePeriod() {
        ensureActive();

        this.currentStreak++;
        this.bestStreak = Math.max(
                this.bestStreak,
                this.currentStreak
        );
    }

    public void startNewStreak() {
        ensureActive();

        this.currentStreak = 1;
        this.bestStreak = Math.max(
                this.bestStreak,
                this.currentStreak
        );
    }

    public void applyRecalculatedStreaks(int currentStreak, int bestStreak) {
        validateStreaks(currentStreak, bestStreak);
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
    }

    private void ensureActive() {
        if (!active) {
            throw new InactiveHabitException(id);
        }
    }

    private void validateStreaks(int currentStreak, int bestStreak) {
        if (currentStreak < 0 || bestStreak < 0) {
            throw new IllegalArgumentException(
                    "Streak values cannot be negative"
            );
        }

        if (bestStreak < currentStreak) {
            throw new IllegalArgumentException(
                    "Best streak cannot be smaller than current streak"
            );
        }
    }

}
