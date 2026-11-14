package com.example.Habits.business.HabitCompletion;

import java.time.LocalDate;

public interface IUncompleteHabit {
    void uncompleteHabit(Long habitId, Long userId);

    /** Undoes the completion on the given date (the date's week for weekly habits). */
    void uncompleteHabit(Long habitId, Long userId, LocalDate date);
}
