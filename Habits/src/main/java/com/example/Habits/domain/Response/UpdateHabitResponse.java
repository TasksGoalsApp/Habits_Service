package com.example.Habits.domain.Response;

import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.TimeOfDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateHabitResponse {

    private String name;
    private HabitFrequency habitFrequency;
    private HabitCategory habitCategory;
    private String description;
    private Set<DayOfWeek> weekdays;
    private TimeOfDay timeOfDay;
    private String target;
}
