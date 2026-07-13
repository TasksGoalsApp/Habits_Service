package com.example.Habits.business;

import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Response.CreateHabitResponse;

public interface ICreateHabit {
    CreateHabitResponse createHabit(CreateHabitRequest request, Long user_Id);
}
