package com.example.Habits.domain.Request;

import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.TimeOfDay;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.Set;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UpdateHabitRequest {

    @NotBlank
    @Size(max = 100)
    private String name;
    @NotNull
    private HabitFrequency habitFrequency;
    @NotNull
    private HabitCategory habitCategory;

    @Size(max = 280)
    private String description;
    /** Scheduled days (MONDAY..SUNDAY). Null or empty means every day. */
    private Set<@NotNull DayOfWeek> weekdays;
    /** Null means ANYTIME. */
    private TimeOfDay timeOfDay;
    @Size(max = 40)
    private String target;
}
