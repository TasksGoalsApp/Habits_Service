package com.example.Habits.domain;

import com.example.Habits.exception.InactiveHabitException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HabitTest {
    @Test
    void activate_shouldSetHabitAsActive() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(false)
                .build();

        habit.activate();

        assertThat(habit.isActive()).isTrue();
    }

    @Test
    void deactivate_shouldSetHabitAsInactive() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(true)
                .build();

        habit.deactivate();

        assertThat(habit.isActive()).isFalse();
    }

    @Test
    void completeConsecutivePeriod_shouldIncrementCurrentStreak() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(true)
                .currentStreak(3)
                .bestStreak(7)
                .build();

        habit.completeConsecutivePeriod();

        assertThat(habit.getCurrentStreak()).isEqualTo(4);
        assertThat(habit.getBestStreak()).isEqualTo(7);
    }
    @Test
    void completeConsecutivePeriod_shouldUpdateBestStreak_whenCurrentExceedsBest() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(true)
                .currentStreak(4)
                .bestStreak(4)
                .build();

        habit.completeConsecutivePeriod();

        assertThat(habit.getCurrentStreak()).isEqualTo(5);
        assertThat(habit.getBestStreak()).isEqualTo(5);
    }

    @Test
    void completeConsecutivePeriod_shouldThrow_whenHabitIsInactive() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(false)
                .currentStreak(3)
                .bestStreak(5)
                .build();

        assertThatThrownBy(habit::completeConsecutivePeriod)
                .isInstanceOf(InactiveHabitException.class);

        assertThat(habit.getCurrentStreak()).isEqualTo(3);
        assertThat(habit.getBestStreak()).isEqualTo(5);
    }

    @Test
    void startNewStreak_shouldSetCurrentStreakToOne() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(true)
                .currentStreak(5)
                .bestStreak(10)
                .build();

        habit.startNewStreak();

        assertThat(habit.getCurrentStreak()).isEqualTo(1);
        assertThat(habit.getBestStreak()).isEqualTo(10);
    }

    @Test
    void startNewStreak_shouldSetBestStreakToOne_whenHabitHasNoPreviousStreak() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(true)
                .currentStreak(0)
                .bestStreak(0)
                .build();

        habit.startNewStreak();

        assertThat(habit.getCurrentStreak()).isEqualTo(1);
        assertThat(habit.getBestStreak()).isEqualTo(1);
    }

    @Test
    void startNewStreak_shouldThrow_whenHabitIsInactive() {
        Habit habit = Habit.builder()
                .id(1L)
                .active(false)
                .build();

        assertThatThrownBy(habit::startNewStreak)
                .isInstanceOf(InactiveHabitException.class);
    }

    @Test
    void applyRecalculatedStreaks_shouldUpdateBothStreakValues() {
        Habit habit = Habit.builder()
                .currentStreak(2)
                .bestStreak(8)
                .build();

        habit.applyRecalculatedStreaks(4, 10);

        assertThat(habit.getCurrentStreak()).isEqualTo(4);
        assertThat(habit.getBestStreak()).isEqualTo(10);
    }

    @Test
    void applyRecalculatedStreaks_shouldRejectNegativeCurrentStreak() {
        Habit habit = Habit.builder().build();

        assertThatThrownBy(() ->
                habit.applyRecalculatedStreaks(-1, 5)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void applyRecalculatedStreaks_shouldRejectNegativeBestStreak() {
        Habit habit = Habit.builder().build();

        assertThatThrownBy(() ->
                habit.applyRecalculatedStreaks(0, -1)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void applyRecalculatedStreaks_shouldRejectBestStreakSmallerThanCurrent() {
        Habit habit = Habit.builder().build();

        assertThatThrownBy(() ->
                habit.applyRecalculatedStreaks(5, 3)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
