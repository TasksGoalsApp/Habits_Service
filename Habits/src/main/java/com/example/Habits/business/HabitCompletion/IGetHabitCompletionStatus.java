package com.example.Habits.business.HabitCompletion;

import com.example.Habits.domain.Response.HabitCompletion.HabitCompletionStatusResponse;

public interface IGetHabitCompletionStatus {
    HabitCompletionStatusResponse getStatus(Long habitId, Long userId);
}
