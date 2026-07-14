package com.example.Habits.business.HabitCompletion;

import com.example.Habits.domain.Response.HabitCompletion.CompleteHabitResponse;

public interface ICompleteHabit {
    CompleteHabitResponse completeHabit (Long habitId, Long userId);
}
