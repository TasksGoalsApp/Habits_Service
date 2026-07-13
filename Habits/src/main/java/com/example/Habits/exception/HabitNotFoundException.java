package com.example.Habits.exception;

public class HabitNotFoundException extends RuntimeException {

    public HabitNotFoundException(Long habitId) {

        super("Habit with ID " + habitId + " was not found");
    }
}
