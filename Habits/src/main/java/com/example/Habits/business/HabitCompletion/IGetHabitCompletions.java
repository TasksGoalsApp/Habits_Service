package com.example.Habits.business.HabitCompletion;

import com.example.Habits.domain.Response.HabitCompletion.GetHabitCompletionResponse;

public interface IGetHabitCompletions {
 GetHabitCompletionResponse getCompletions(Long habitId, Long userId);
}
