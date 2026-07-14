package com.example.Habits.business.Impl.HabitCompletion;

import com.example.Habits.business.HabitCompletion.IGetHabitCompletions;
import com.example.Habits.domain.Response.HabitCompletion.GetHabitCompletionResponse;
import com.example.Habits.domain.Response.HabitCompletion.HabitCompletionResponse;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class GetHabitCompletionsImpl implements IGetHabitCompletions {
    private final HabitsRepository habitsRepository;
    private final HabitCompletionRepository habitCompletionRepository;

    @Transactional
    @Override
    public GetHabitCompletionResponse getCompletions(Long habitId, Long userId) {
        habitsRepository.findByIdAndUserId(habitId, userId)
                .orElseThrow(() -> new HabitNotFoundException(habitId));

        List<HabitCompletionResponse> completions = habitCompletionRepository
                        .findAllByHabitIdOrderByCompletionDateDesc(habitId)
                        .stream()
                        .map(entity ->
                                HabitCompletionResponse.builder()
                                        .id(entity.getId())
                                        .completionDate(
                                                entity.getCompletionDate()
                                        )
                                        .build()
                        )
                        .toList();

        return GetHabitCompletionResponse.builder()
                .habitId(habitId)
                .completions(completions)
                .build();
    }
}
