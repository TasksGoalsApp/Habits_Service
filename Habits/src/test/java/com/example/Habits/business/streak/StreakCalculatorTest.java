package com.example.Habits.business.streak;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;

public class StreakCalculatorTest {
    private final StreakCalculator streakCalculator = new StreakCalculator();

    @Test
    void calculateDaily_shouldReturnZeroStreaks_whenThereAreNoCompletions() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        StreakResult result = streakCalculator.calculateDaily(List.of(), today);

        assertThat(result.currentStreak()).isZero();
        assertThat(result.bestStreak()).isZero();
    }

    @Test
    void calculateDaily_shouldReturnOneForSingleCompletionToday() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        StreakResult result = streakCalculator.calculateDaily(List.of(today), today);

        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.bestStreak()).isEqualTo(1);
    }

    @Test
    void calculateDaily_shouldCalculateConsecutiveDailyStreak() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                today.minusDays(3),
                today.minusDays(2),
                today.minusDays(1),
                today
        );

        StreakResult result = streakCalculator.calculateDaily(completions, today);

        assertThat(result.currentStreak()).isEqualTo(4);
        assertThat(result.bestStreak()).isEqualTo(4);
    }

    @Test
    void calculateDaily_shouldCalculateBestStreakAcrossBrokenSequences() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 3),
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 11)
        );

        StreakResult result = streakCalculator.calculateDaily(completions, today);

        assertThat(result.currentStreak()).isZero();
        assertThat(result.bestStreak()).isEqualTo(3);
    }

    @Test
    void calculateDaily_shouldKeepCurrentStreakActive_whenLastCompletionWasYesterday() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                today.minusDays(3),
                today.minusDays(2),
                today.minusDays(1)
        );

        StreakResult result = streakCalculator.calculateDaily(completions, today);

        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.bestStreak()).isEqualTo(3);
    }

    @Test
    void calculateDaily_shouldReturnZeroCurrentStreak_whenLastCompletionIsTooOld() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                today.minusDays(4),
                today.minusDays(3)
        );

        StreakResult result = streakCalculator.calculateDaily(completions, today);

        assertThat(result.currentStreak()).isZero();
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateDaily_shouldIgnoreDuplicateDates() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                today.minusDays(1),
                today.minusDays(1),
                today
        );

        StreakResult result = streakCalculator.calculateDaily(completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateDaily_shouldHandleUnsortedDates() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                today,
                today.minusDays(2),
                today.minusDays(1)
        );

        StreakResult result = streakCalculator.calculateDaily(completions, today);

        assertThat(result.currentStreak()).isEqualTo(3);
        assertThat(result.bestStreak()).isEqualTo(3);
    }

    //WEEKLY CALCULATIONS FROM HERE:

    @Test
    void calculateWeekly_shouldReturnZeroStreaks_whenThereAreNoCompletions() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        StreakResult result = streakCalculator.calculateWeekly(List.of(), today);

        assertThat(result.currentStreak()).isZero();
        assertThat(result.bestStreak()).isZero();
    }

    @Test
    void calculateWeekly_shouldReturnOneForCompletionInCurrentWeek() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        StreakResult result = streakCalculator.calculateWeekly(List.of(LocalDate.of(2026, 7, 14)), today);

        assertThat(result.currentStreak()).isEqualTo(1);
        assertThat(result.bestStreak()).isEqualTo(1);
    }

    @Test
    void calculateWeekly_shouldCalculateConsecutiveWeeks() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 6, 23),
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 14)
        );

        StreakResult result = streakCalculator.calculateWeekly(completions, today);

        assertThat(result.currentStreak()).isEqualTo(4);
        assertThat(result.bestStreak()).isEqualTo(4);
    }

    @Test
    void calculateWeekly_shouldResetSequenceAfterSkippedWeek() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 22),
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 14)
        );

        StreakResult result = streakCalculator.calculateWeekly(completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateWeekly_shouldKeepCurrentStreakActive_whenLastCompletionWasPreviousWeek() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 6, 29),
                LocalDate.of(2026, 7, 7)
        );

        StreakResult result = streakCalculator.calculateWeekly(completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateWeekly_shouldReturnZeroCurrentStreak_whenLastCompletionIsTooOld() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 22)
        );

        StreakResult result = streakCalculator.calculateWeekly(completions, today);

        assertThat(result.currentStreak()).isZero();
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateWeekly_shouldCountOnlyOneCompletionPerWeek() {
        LocalDate today = LocalDate.of(2026, 7, 15);

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 8),
                LocalDate.of(2026, 7, 14)
        );

        StreakResult result = streakCalculator.calculateWeekly(completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }
}
