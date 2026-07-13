package com.example.Habits.business;

import com.example.Habits.domain.Request.UpdateHabitRequest;
import com.example.Habits.domain.Response.UpdateHabitResponse;

public interface IUpdateHabit {
    UpdateHabitResponse updateHabit(UpdateHabitRequest request, Long habit_id, Long user_id);
}
