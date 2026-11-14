package com.example.Habits.business.Impl.HabitCompletion;

import com.example.Habits.business.HabitCompletion.ICompleteHabit;
import com.example.Habits.business.HabitConverter;
import com.example.Habits.business.streak.StreakCalculator;
import com.example.Habits.business.streak.StreakResult;
import com.example.Habits.domain.Habit;
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
import java.util.List;

@AllArgsConstructor
@Service
public class CompleteHabitImpl implements ICompleteHabit {

    private final HabitsRepository habitsRepository;
    private final HabitCompletionRepository habitCompletionRepository;
    private final StreakCalculator streakCalculator;

    @Override
    public CompleteHabitResponse completeHabit(Long habitId, Long userId) {
        return completeHabit(habitId, userId, null);
    }

    @Transactional
    @Override
    public CompleteHabitResponse completeHabit(Long habitId, Long userId, LocalDate date) {
        HabitEntity habitEntity = habitsRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        Habit habit = HabitConverter.toDomain(habitEntity);

        LocalDate today = LocalDate.now();
        LocalDate completionDate = date == null ? today : date;

        validateDate(habit, completionDate, today);

        if (!habit.isActive()) {
            throw new InactiveHabitException(habitId);
        }

        validateNotAlreadyCompleted(habit, completionDate);

        HabitCompletionEntity completion = habitCompletionRepository.save(
                        HabitCompletionEntity.builder()
                                .habitId(habitId)
                                .completionDate(completionDate)
                                .build()
                );

        recalculateStreaks(habit, habitEntity, today);

        return CompleteHabitResponse.builder()
                .completionId(completion.getId())
                .habitId(habitId)
                .completionDate(completionDate)
                .currentStreak(habit.getCurrentStreak())
                .bestStreak(habit.getBestStreak())
                .build();
    }

    private void validateDate(Habit habit, LocalDate completionDate, LocalDate today) {
        if (completionDate.isAfter(today)) {
            throw new IllegalArgumentException("Completion date cannot be in the future");
        }

        if (habit.getCreatedAt() != null && completionDate.isBefore(habit.getCreatedAt().toLocalDate())) {
            throw new IllegalArgumentException("Completion date cannot be before the habit was created");
        }
    }

    private void validateNotAlreadyCompleted(Habit habit, LocalDate completionDate) {
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

    private void recalculateStreaks(Habit habit, HabitEntity habitEntity, LocalDate today) {
        List<LocalDate> completionDates = habitCompletionRepository
                .findAllByHabitIdOrderByCompletionDateAsc(habit.getId())
                .stream()
                .map(HabitCompletionEntity::getCompletionDate)
                .toList();

        StreakResult result = streakCalculator.calculate(habit.getHabitFrequency(), habit.getWeekdays(), completionDates, today);

        habit.applyRecalculatedStreaks(result.currentStreak(), result.bestStreak());

        HabitConverter.applyToEntity(habit, habitEntity);
    }
}
