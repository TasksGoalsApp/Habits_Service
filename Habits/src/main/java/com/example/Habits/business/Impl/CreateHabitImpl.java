package com.example.Habits.business.Impl;

import com.example.Habits.business.ICreateHabit;
import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Response.CreateHabitResponse;
import com.example.Habits.repository.HabitsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateHabitImpl implements ICreateHabit {
    private final HabitsRepository repository;

    @Override
    public CreateHabitResponse createHabit(CreateHabitRequest request) {
        return null;
    }
}
