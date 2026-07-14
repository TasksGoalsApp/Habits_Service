package com.example.Habits.exception;

public class InactiveHabitException extends RuntimeException {
    public InactiveHabitException(Long habitId) {
        super("Habit with ID " + habitId + " is inactive and cannot be completed");
    }
}
