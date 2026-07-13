package com.example.Habits.domain.Request;

import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
