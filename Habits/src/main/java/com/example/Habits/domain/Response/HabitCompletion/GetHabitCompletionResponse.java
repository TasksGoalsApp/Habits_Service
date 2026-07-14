package com.example.Habits.domain.Response.HabitCompletion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetHabitCompletionResponse {
    private Long habitId;
    private List<HabitCompletionResponse> completions;
}
