package com.example.Habits.business.streak;

import com.example.Habits.domain.HabitFrequency;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
@Component
public class StreakCalculator {

    public StreakResult calculate(HabitFrequency habitFrequency, List<LocalDate> completionDates, LocalDate currentDate){
        return calculate(habitFrequency, null, completionDates, currentDate);
    }

    /** Weekdays only matter for DAILY habits; null or empty means every day. */
    public StreakResult calculate(HabitFrequency habitFrequency, Set<DayOfWeek> weekdays, List<LocalDate> completionDates, LocalDate currentDate){
        if (habitFrequency == HabitFrequency.DAILY){
            return calculateDaily(weekdays, completionDates, currentDate);
        }
        return calculateWeekly(completionDates, currentDate);
    }

    /**
     * Streak over scheduled days only: consecutive scheduled days extend it, unscheduled days
     * neither break nor extend it, and completions on unscheduled days are ignored.
     */
    public StreakResult calculateDaily(Set<DayOfWeek> weekdays, List<LocalDate> completionDates, LocalDate currentDate) {
        if (weekdays == null || weekdays.isEmpty() || weekdays.size() == DayOfWeek.values().length) {
            return calculateDaily(completionDates, currentDate);
        }

        List<LocalDate> sortedDates = completionDates.stream()
                .filter(date -> weekdays.contains(date.getDayOfWeek()))
                .distinct()
                .sorted()
                .toList();

        if (sortedDates.isEmpty()) {
            return new StreakResult(0, 0);
        }

        int runningStreak = 1;
        int bestStreak = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            if (sortedDates.get(i - 1).equals(previousScheduledDay(weekdays, sortedDates.get(i)))) {
                runningStreak++;
            } else {
                runningStreak = 1;
            }

            bestStreak = Math.max(bestStreak, runningStreak);
        }

        LocalDate lastCompletion = sortedDates.get(sortedDates.size() - 1);

        boolean currentStreakIsActive = lastCompletion.equals(currentDate)
                || lastCompletion.equals(previousScheduledDay(weekdays, currentDate));

        return new StreakResult(currentStreakIsActive ? runningStreak : 0, bestStreak);
    }

    private LocalDate previousScheduledDay(Set<DayOfWeek> weekdays, LocalDate date) {
        LocalDate candidate = date.minusDays(1);
        while (!weekdays.contains(candidate.getDayOfWeek())) {
            candidate = candidate.minusDays(1);
        }
        return candidate;
    }



    public StreakResult calculateDaily(List<LocalDate> completionDates, LocalDate currentDate) {
        if (completionDates.isEmpty()) {
            return new StreakResult(0, 0);
        }

        List<LocalDate> sortedDates = completionDates.stream()
                .distinct()
                .sorted()
                .toList();

        int runningStreak = 1;
        int bestStreak = 1;

        for (int i = 1; i < sortedDates.size(); i++) {
            LocalDate previousDate = sortedDates.get(i - 1);
            LocalDate date = sortedDates.get(i);

            if (date.equals(previousDate.plusDays(1))) {
                runningStreak++;
            } else {
                runningStreak = 1;
            }

            bestStreak = Math.max(bestStreak, runningStreak);
        }

        LocalDate lastCompletion = sortedDates.get(sortedDates.size() - 1);

        boolean currentStreakIsActive = lastCompletion.equals(currentDate) || lastCompletion.equals(currentDate.minusDays(1));

        return new StreakResult(currentStreakIsActive ? runningStreak : 0, bestStreak);
    }

    public StreakResult calculateWeekly(List<LocalDate> completionDates, LocalDate currentDate) {
        if (completionDates.isEmpty()) {
            return new StreakResult(0, 0);
        }

        List<LocalDate> completionWeeks = completionDates.stream()
                .map(this::startOfWeek)
                .distinct()
                .sorted()
                .toList();

        int runningStreak = 1;
        int bestStreak = 1;

        for (int i = 1; i < completionWeeks.size(); i++) {
            LocalDate previousWeek = completionWeeks.get(i - 1);
            LocalDate week = completionWeeks.get(i);

            if (week.equals(previousWeek.plusWeeks(1))) {
                runningStreak++;
            } else {
                runningStreak = 1;
            }

            bestStreak = Math.max(bestStreak, runningStreak);
        }

        LocalDate currentWeek = startOfWeek(currentDate);
        LocalDate lastCompletionWeek = completionWeeks.get(completionWeeks.size() - 1);

        boolean currentStreakIsActive = lastCompletionWeek.equals(currentWeek) || lastCompletionWeek.equals(currentWeek.minusWeeks(1));

        return new StreakResult(currentStreakIsActive ? runningStreak : 0, bestStreak);
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
