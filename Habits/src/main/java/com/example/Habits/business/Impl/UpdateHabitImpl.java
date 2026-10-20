package com.example.Habits.business.Impl;

import com.example.Habits.business.HabitText;
import com.example.Habits.business.IUpdateHabit;
import com.example.Habits.domain.TimeOfDay;
import com.example.Habits.domain.Weekdays;
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

        habitEntity.setName(request.getName().trim());
        habitEntity.setHabitCategory(request.getHabitCategory());
        habitEntity.setHabitFrequency(request.getHabitFrequency());
        habitEntity.setDescription(HabitText.clean(request.getDescription()));
        habitEntity.setWeekdays(Weekdays.normalize(request.getWeekdays()));
        habitEntity.setTimeOfDay(request.getTimeOfDay() == null ? TimeOfDay.ANYTIME : request.getTimeOfDay());
        habitEntity.setTarget(HabitText.clean(request.getTarget()));

        return UpdateHabitResponse.builder()
                .name(habitEntity.getName())
                .habitFrequency(habitEntity.getHabitFrequency())
                .habitCategory(habitEntity.getHabitCategory())
                .description(habitEntity.getDescription())
                .weekdays(habitEntity.getWeekdays())
                .timeOfDay(habitEntity.getTimeOfDay())
                .target(habitEntity.getTarget())
                .build();
    }
}
