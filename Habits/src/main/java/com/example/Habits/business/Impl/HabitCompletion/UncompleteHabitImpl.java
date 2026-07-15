package com.example.Habits.business.Impl.HabitCompletion;

import com.example.Habits.business.HabitCompletion.IUncompleteHabit;
import com.example.Habits.business.HabitConverter;
import com.example.Habits.domain.Habit;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.exception.HabitNotCompletedException;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitCompletionEntity;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UncompleteHabitImpl implements IUncompleteHabit {

    private final HabitsRepository habitsRepository;
    private final HabitCompletionRepository habitCompletionRepository;
    private final HabitConverter habitConverter;
    private record StreakResult(int currentStreak, int bestStreak) {}

    @Transactional
    @Override
    public void uncompleteHabit(Long habitId, Long userId) {

        HabitEntity habit = habitsRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        LocalDate today = LocalDate.now();

        HabitCompletionEntity completion = findCurrentCompletion(habit, today)
                        .orElseThrow(() -> new HabitNotCompletedException(habitId));

        habitCompletionRepository.delete(completion);
        habitCompletionRepository.flush();

        recalculateStreaks(habit);
    }

    private Optional<HabitCompletionEntity> findCurrentCompletion(HabitEntity habit, LocalDate currentDate) {
        if (habit.getHabitFrequency() == HabitFrequency.DAILY) {
            return habitCompletionRepository.findByHabitIdAndCompletionDate(habit.getId(),currentDate);
        }

        LocalDate weekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        return habitCompletionRepository.findFirstByHabitIdAndCompletionDateBetween(habit.getId(), weekStart, weekEnd);
    }

    private void recalculateStreaks(HabitEntity habitEntity) {
        List<HabitCompletionEntity> completions = habitCompletionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitEntity.getId());

        Habit habit = habitConverter.toDomain(habitEntity);

        if (completions.isEmpty()) {
            habit.applyRecalculatedStreaks(0, 0);
            habitConverter.applyToEntity(habit, habitEntity);
            return;
        }

        StreakResult streakResult;

        if (habit.getHabitFrequency() == HabitFrequency.DAILY) {
            streakResult = calculateDailyStreaks(completions);
        } else {
            streakResult = calculateWeeklyStreaks(completions);
        }

        habit.applyRecalculatedStreaks(
                streakResult.currentStreak(),
                streakResult.bestStreak()
        );

        habitConverter.applyToEntity(habit, habitEntity);
    }

    private StreakResult calculateDailyStreaks(List<HabitCompletionEntity> completions) {
        int runningStreak = 1;
        int bestStreak = 1;

        for (int i = 1; i < completions.size(); i++) {
            LocalDate previousDate = completions.get(i - 1).getCompletionDate();

            LocalDate currentDate = completions.get(i).getCompletionDate();

            if (currentDate.equals(previousDate.plusDays(1))) {
                runningStreak++;
            } else {
                runningStreak = 1;
            }

            bestStreak = Math.max(bestStreak, runningStreak);
        }

        LocalDate lastCompletionDate = completions.get(completions.size() - 1).getCompletionDate();

        LocalDate today = LocalDate.now();

        boolean currentStreakIsActive = lastCompletionDate.equals(today) || lastCompletionDate.equals(today.minusDays(1));

        int currentStreak = currentStreakIsActive ? runningStreak : 0;

        return new StreakResult(currentStreak, bestStreak);
    }

    private StreakResult calculateWeeklyStreaks(List<HabitCompletionEntity> completions) {
        List<LocalDate> completionWeeks = completions.stream()
                .map(HabitCompletionEntity::getCompletionDate)
                .map(this::getWeekStart)
                .distinct()
                .sorted()
                .toList();

        int runningStreak = 1;
        int bestStreak = 1;

        for (int i = 1; i < completionWeeks.size(); i++) {
            LocalDate previousWeek = completionWeeks.get(i - 1);

            LocalDate currentWeek = completionWeeks.get(i);

            if (currentWeek.equals(previousWeek.plusWeeks(1))) {
                runningStreak++;
            } else {
                runningStreak = 1;
            }

            bestStreak = Math.max(bestStreak, runningStreak);
        }

        LocalDate currentWeekStart = getWeekStart(LocalDate.now());

        LocalDate lastCompletionWeek = completionWeeks.get(completionWeeks.size() - 1);

        boolean currentStreakIsActive = lastCompletionWeek.equals(currentWeekStart) || lastCompletionWeek.equals(currentWeekStart.minusWeeks(1));

        int currentStreak = currentStreakIsActive ? runningStreak : 0;

        return new StreakResult(currentStreak, bestStreak);
    }

    private LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

}
