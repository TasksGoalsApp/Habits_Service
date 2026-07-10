package com.example.Habits.business.Impl;

import com.example.Habits.business.IUpdateHabit;
import com.example.Habits.domain.Request.UpdateHabitRequest;
import com.example.Habits.domain.Response.UpdateHabitResponse;
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
    public UpdateHabitResponse updateHabit(UpdateHabitRequest request) {
        return null;
    }
}
