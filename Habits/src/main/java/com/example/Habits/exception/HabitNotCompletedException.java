package com.example.Habits.exception;

public class HabitNotCompletedException extends RuntimeException {
    public HabitNotCompletedException(Long habitId) {
        super("Habit with ID " + habitId + " is not completed for the current period");
    }
}
