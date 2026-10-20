package com.example.Habits.business.Impl;

import com.example.Habits.business.HabitText;
import com.example.Habits.business.ICreateHabit;
import com.example.Habits.domain.TimeOfDay;
import com.example.Habits.domain.Weekdays;
import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Response.CreateHabitResponse;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateHabitImpl implements ICreateHabit {
    private final HabitsRepository repository;

    @Override
    public CreateHabitResponse createHabit(CreateHabitRequest request, Long user_id) {

        HabitEntity savedHabit = saveHabit(request, user_id);

        return CreateHabitResponse.builder()
                .id(savedHabit.getId())
                .description(savedHabit.getDescription())
                .weekdays(savedHabit.getWeekdays())
                .timeOfDay(savedHabit.getTimeOfDay())
                .target(savedHabit.getTarget())
                .build();
    }


    private HabitEntity saveHabit(CreateHabitRequest request, Long user_id){
        HabitEntity entity = HabitEntity.builder()
                .userId(user_id)
                .name(request.getName().trim())
                .habitFrequency(request.getHabitFrequency())
                .habitCategory(request.getHabitCategory())
                .description(HabitText.clean(request.getDescription()))
                .weekdays(Weekdays.normalize(request.getWeekdays()))
                .timeOfDay(request.getTimeOfDay() == null ? TimeOfDay.ANYTIME : request.getTimeOfDay())
                .target(HabitText.clean(request.getTarget()))
                .build();

        return repository.save(entity);
    }
}
