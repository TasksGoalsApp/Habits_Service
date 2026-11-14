package com.example.Habits.domain.Response;

import com.example.Habits.domain.TimeOfDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.Set;


@Builder
@Data
public class CreateHabitResponse {
    private Long id;
    private String description;
    private Set<DayOfWeek> weekdays;
    private TimeOfDay timeOfDay;
    private String target;
}
