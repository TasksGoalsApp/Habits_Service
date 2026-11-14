package com.example.Habits.business.streak;

import com.example.Habits.domain.HabitFrequency;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

    // ---- weekday-aware daily streaks (2026-10-19 is a Monday) ----

    private static final Set<DayOfWeek> MON_WED_FRI = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);

    @Test
    void calculateDaily_withWeekdays_shouldCountConsecutiveScheduledDays() {
        LocalDate today = LocalDate.of(2026, 10, 19); // Monday

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 10, 12), // Mon
                LocalDate.of(2026, 10, 14), // Wed
                LocalDate.of(2026, 10, 16), // Fri
                today
        );

        StreakResult result = streakCalculator.calculateDaily(MON_WED_FRI, completions, today);

        assertThat(result.currentStreak()).isEqualTo(4);
        assertThat(result.bestStreak()).isEqualTo(4);
    }

    @Test
    void calculateDaily_withWeekdays_shouldBreakStreak_whenAScheduledDayIsMissed() {
        LocalDate today = LocalDate.of(2026, 10, 19);

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 10, 12), // Mon
                LocalDate.of(2026, 10, 16), // Fri, Wed 14 was missed
                today
        );

        StreakResult result = streakCalculator.calculateDaily(MON_WED_FRI, completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateDaily_withWeekdays_shouldKeepCurrentStreakAlive_whenTodayIsScheduledButNotDoneYet() {
        LocalDate today = LocalDate.of(2026, 10, 19); // Monday, not done yet

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 10, 14), // Wed
                LocalDate.of(2026, 10, 16)  // Fri
        );

        StreakResult result = streakCalculator.calculateDaily(MON_WED_FRI, completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateDaily_withWeekdays_shouldKeepCurrentStreakAlive_whenTodayIsNotScheduled() {
        LocalDate today = LocalDate.of(2026, 10, 17); // Saturday

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 10, 14), // Wed
                LocalDate.of(2026, 10, 16)  // Fri
        );

        StreakResult result = streakCalculator.calculateDaily(MON_WED_FRI, completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateDaily_withWeekdays_shouldResetCurrentStreak_whenLastScheduledDayWasMissed() {
        LocalDate today = LocalDate.of(2026, 10, 19); // Monday, Fri 16 was missed

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 10, 12), // Mon
                LocalDate.of(2026, 10, 14)  // Wed
        );

        StreakResult result = streakCalculator.calculateDaily(MON_WED_FRI, completions, today);

        assertThat(result.currentStreak()).isZero();
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateDaily_withWeekdays_shouldIgnoreCompletionsOnUnscheduledDays() {
        LocalDate today = LocalDate.of(2026, 10, 14); // Wednesday

        List<LocalDate> completions = List.of(
                LocalDate.of(2026, 10, 12), // Mon
                LocalDate.of(2026, 10, 13), // Tue, not scheduled
                today
        );

        StreakResult result = streakCalculator.calculateDaily(MON_WED_FRI, completions, today);

        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.bestStreak()).isEqualTo(2);
    }

    @Test
    void calculateDaily_withWeekdays_shouldReturnZero_whenOnlyUnscheduledDaysWereCompleted() {
        LocalDate today = LocalDate.of(2026, 10, 14);

        StreakResult result = streakCalculator.calculateDaily(MON_WED_FRI, List.of(LocalDate.of(2026, 10, 13)), today);

        assertThat(result.currentStreak()).isZero();
        assertThat(result.bestStreak()).isZero();
    }

    @Test
    void calculateDaily_withNullOrEmptyWeekdays_shouldBehaveLikeEveryDay() {
        LocalDate today = LocalDate.of(2026, 10, 19);
        List<LocalDate> completions = List.of(today.minusDays(2), today.minusDays(1), today);

        assertThat(streakCalculator.calculateDaily(null, completions, today))
                .isEqualTo(streakCalculator.calculateDaily(completions, today));
        assertThat(streakCalculator.calculateDaily(Set.of(), completions, today))
                .isEqualTo(streakCalculator.calculateDaily(completions, today));
    }

    @Test
    void calculate_shouldIgnoreWeekdaysForWeeklyHabits() {
        LocalDate today = LocalDate.of(2026, 10, 19);
        List<LocalDate> completions = List.of(LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 14)); // Wed, Wed

        StreakResult result = streakCalculator.calculate(HabitFrequency.WEEKLY, EnumSet.of(DayOfWeek.MONDAY), completions, today);

        assertThat(result).isEqualTo(streakCalculator.calculateWeekly(completions, today));
        assertThat(result.currentStreak()).isEqualTo(2);
    }
}
