package com.example.Habits.exception;

public class HabitAlreadyCompletedException extends RuntimeException {
    public HabitAlreadyCompletedException(Long habitId) {
        super("Habit with ID " + habitId + " is already completed for the current period");
    }
}
