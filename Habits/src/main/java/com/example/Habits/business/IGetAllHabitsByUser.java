package com.example.Habits.business;

import com.example.Habits.domain.Response.GetAllHabitsByUserResponse;

public interface IGetAllHabitsByUser {
    GetAllHabitsByUserResponse getAllHabits(Long userId);

}
