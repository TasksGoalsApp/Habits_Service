package com.example.Habits.business.Impl.HabitCompletion;

import com.example.Habits.business.HabitCompletion.ICompleteHabit;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Response.HabitCompletion.CompleteHabitResponse;
import com.example.Habits.exception.HabitAlreadyCompletedException;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.exception.InactiveHabitException;
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
import java.util.Optional;

@AllArgsConstructor
@Service
public class CompleteHabitImpl implements ICompleteHabit {

    private final HabitsRepository habitsRepository;
    private final HabitCompletionRepository habitCompletionRepository;

    @Transactional
    @Override
    public CompleteHabitResponse completeHabit(Long habitId, Long userId) {
        HabitEntity habit = habitsRepository
                .findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        if (!habit.isActive()) {
            throw new InactiveHabitException(habitId);
        }

        LocalDate today = LocalDate.now();

        validateNotAlreadyCompleted(habit, today);

        Optional<HabitCompletionEntity> previousCompletion =
                habitCompletionRepository
                        .findTopByHabitIdOrderByCompletionDateDesc(habitId);

        updateStreak(habit, previousCompletion, today);

        HabitCompletionEntity completion =
                habitCompletionRepository.save(
                        HabitCompletionEntity.builder()
                                .habitId(habitId)
                                .completionDate(today)
                                .build()
                );

        return CompleteHabitResponse.builder()
                .completionId(completion.getId())
                .habitId(habitId)
                .completionDate(today)
                .currentStreak(habit.getCurrentStreak())
                .bestStreak(habit.getBestStreak())
                .build();
    }

    private void validateNotAlreadyCompleted(HabitEntity habit, LocalDate completionDate) {
        boolean alreadyCompleted;

        if (habit.getHabitFrequency() == HabitFrequency.DAILY) {
            alreadyCompleted = habitCompletionRepository.existsByHabitIdAndCompletionDate(habit.getId(), completionDate);
        } else {
            LocalDate weekStart = completionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            LocalDate weekEnd = weekStart.plusDays(6);

            alreadyCompleted = habitCompletionRepository .existsByHabitIdAndCompletionDateBetween(habit.getId(), weekStart, weekEnd);
        }

        if (alreadyCompleted) {
            throw new HabitAlreadyCompletedException(habit.getId());
        }
    }


    private void updateStreak(HabitEntity habit, Optional<HabitCompletionEntity> previousCompletion, LocalDate currentDate) {
        if (previousCompletion.isEmpty()) {
            habit.setCurrentStreak(1);
            habit.setBestStreak(Math.max(habit.getBestStreak(), 1));
            return;
        }

        LocalDate previousDate = previousCompletion.get().getCompletionDate();

        boolean consecutive =
                habit.getHabitFrequency() == HabitFrequency.DAILY ? previousDate.equals(currentDate.minusDays(1)) : isPreviousIsoWeek(previousDate, currentDate);

        if (consecutive) {
            habit.setCurrentStreak(habit.getCurrentStreak() + 1);
        } else {
            habit.setCurrentStreak(1);
        }

        if (habit.getCurrentStreak() > habit.getBestStreak()) {
            habit.setBestStreak(habit.getCurrentStreak());
        }
    }


    private boolean isPreviousIsoWeek(LocalDate previousDate, LocalDate currentDate) {
        LocalDate currentWeekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate previousWeekStart = currentWeekStart.minusWeeks(1);

        LocalDate previousCompletionWeekStart = previousDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return previousCompletionWeekStart.equals(previousWeekStart);
    }
}
