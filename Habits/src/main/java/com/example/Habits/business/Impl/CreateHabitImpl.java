package com.example.Habits.business.Impl;

import com.example.Habits.business.ICreateHabit;
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
                .build();
    }


    private HabitEntity saveHabit(CreateHabitRequest request, Long user_id){
        HabitEntity entity = HabitEntity.builder()
                .userId(user_id)
                .name(request.getName().trim())
                .habitFrequency(request.getHabitFrequency())
                .habitCategory(request.getHabitCategory())
                .build();

        return repository.save(entity);
    }
}
