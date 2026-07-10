package com.example.Habits.domain.Response;

import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateHabitResponse {

    private String name;
    private HabitFrequency habitFrequency;
    private HabitCategory habitCategory;
}
