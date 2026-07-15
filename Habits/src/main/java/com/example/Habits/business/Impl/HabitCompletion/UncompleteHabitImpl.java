package com.example.Habits.business.Impl.HabitCompletion;

import com.example.Habits.business.HabitCompletion.IUncompleteHabit;
import com.example.Habits.business.HabitConverter;
import com.example.Habits.business.streak.StreakCalculator;
import com.example.Habits.business.streak.StreakResult;
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
    private final StreakCalculator streakCalculator;

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

        Habit habit = HabitConverter.toDomain(habitEntity);

        List<LocalDate> completionDates = completions.stream()
                .map(HabitCompletionEntity::getCompletionDate)
                .toList();

        StreakResult result = streakCalculator.calculate(habit.getHabitFrequency(), completionDates,LocalDate.now());

        habit.applyRecalculatedStreaks(result.currentStreak(), result.bestStreak());

        HabitConverter.applyToEntity(habit, habitEntity);
    }


}
