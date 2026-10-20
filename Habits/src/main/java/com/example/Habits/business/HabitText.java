package com.example.Habits.business;

public final class HabitText {
    private HabitText() {}

    /** Trims optional free text; blank becomes null. */
    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
