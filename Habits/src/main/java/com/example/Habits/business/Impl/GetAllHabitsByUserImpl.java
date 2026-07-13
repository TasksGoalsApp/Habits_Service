package com.example.Habits.business.Impl;

import com.example.Habits.business.HabitConverter;
import com.example.Habits.business.IGetAllHabitsByUser;
import com.example.Habits.domain.Habit;
import com.example.Habits.domain.Response.GetAllHabitsByUserResponse;
import com.example.Habits.repository.HabitsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class GetAllHabitsByUserImpl implements IGetAllHabitsByUser {
    private final HabitsRepository habitsRepository;

    @Override
    public GetAllHabitsByUserResponse getAllHabits(Long userId) {
        List<Habit> habits = habitsRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(HabitConverter::convert)
                .toList();

        return GetAllHabitsByUserResponse.builder()
                .habits(habits)
                .build();
    }
}
