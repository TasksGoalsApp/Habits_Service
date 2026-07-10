package com.example.Habits.domain.Response;

import com.example.Habits.domain.Habit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAllHabitsByUserResponse {
 List<Habit> habits;
}
