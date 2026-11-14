package com.example.Habits.business.HabitCompletion;

import com.example.Habits.domain.Response.HabitCompletion.CompleteHabitResponse;

import java.time.LocalDate;

public interface ICompleteHabit {
    CompleteHabitResponse completeHabit (Long habitId, Long userId);

    /** Completes the habit on the given date; a null date means today. */
    CompleteHabitResponse completeHabit (Long habitId, Long userId, LocalDate date);
}
