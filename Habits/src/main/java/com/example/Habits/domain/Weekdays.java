package com.example.Habits.domain;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public final class Weekdays {
    private Weekdays() {}

    public static Set<DayOfWeek> everyDay() {
        return EnumSet.allOf(DayOfWeek.class);
    }

    /** Null or empty means every day. The result is a Monday-first ordered set. */
    public static Set<DayOfWeek> normalize(Collection<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return everyDay();
        }
        return EnumSet.copyOf(days);
    }
}
