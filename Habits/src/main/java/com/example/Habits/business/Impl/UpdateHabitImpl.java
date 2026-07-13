package com.example.Habits.business.Impl;

import com.example.Habits.business.IUpdateHabit;
import com.example.Habits.domain.Request.UpdateHabitRequest;
import com.example.Habits.domain.Response.UpdateHabitResponse;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UpdateHabitImpl implements IUpdateHabit {
    private final HabitsRepository habitsRepository;

    @Transactional
    @Override
    public UpdateHabitResponse updateHabit(UpdateHabitRequest request, Long habit_id, Long user_id) {

        HabitEntity habitEntity = habitsRepository.findByIdAndUserId(habit_id, user_id)
                .orElseThrow(()-> new HabitNotFoundException(habit_id));

        habitEntity.setName(request.getName());
        habitEntity.setHabitCategory(request.getHabitCategory());
        habitEntity.setHabitFrequency(request.getHabitFrequency());

        return UpdateHabitResponse.builder()
                .name(habitEntity.getName())
                .habitFrequency(habitEntity.getHabitFrequency())
                .habitCategory(habitEntity.getHabitCategory())
                .build();
    }
}
