package com.example.Habits.business.Impl.HabitCompletion;

import com.example.Habits.business.HabitCompletion.IGetHabitCompletionStatus;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Response.HabitCompletion.HabitCompletionStatusResponse;
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
import java.util.Optional;

@AllArgsConstructor
@Service
public class GetHabitCompletionStatusImpl implements IGetHabitCompletionStatus {
    private final HabitsRepository habitsRepository;
    private final HabitCompletionRepository habitCompletionRepository;

    @Transactional
    @Override
    public HabitCompletionStatusResponse getStatus(Long habitId, Long userId) {

        HabitEntity habit = habitsRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        LocalDate today = LocalDate.now();

        if (habit.getHabitFrequency() == HabitFrequency.DAILY) {
            return getDailyStatus(habitId, today);
        }

        return getWeeklyStatus(habitId, today);
    }

    private HabitCompletionStatusResponse getDailyStatus(Long habitId, LocalDate today) {
        Optional<HabitCompletionEntity> completion = habitCompletionRepository.findByHabitIdAndCompletionDate(habitId, today);

        return HabitCompletionStatusResponse.builder()
                .habitId(habitId)
                .completed(completion.isPresent())
                .periodStart(today)
                .periodEnd(today)
                .completionDate(
                        completion
                                .map(HabitCompletionEntity::getCompletionDate)
                                .orElse(null)
                )
                .build();
    }


    private HabitCompletionStatusResponse getWeeklyStatus(Long habitId, LocalDate today) {
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        Optional<HabitCompletionEntity> completion = habitCompletionRepository.findFirstByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd);

        return HabitCompletionStatusResponse.builder()
                .habitId(habitId)
                .completed(completion.isPresent())
                .periodStart(weekStart)
                .periodEnd(weekEnd)
                .completionDate(
                        completion
                                .map(HabitCompletionEntity::getCompletionDate)
                                .orElse(null)
                )
                .build();
    }
}
