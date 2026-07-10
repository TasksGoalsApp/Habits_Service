package com.example.Habits.business.Impl;

import com.example.Habits.business.IDeleteHabit;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class DeleteHabitImpl implements IDeleteHabit {
    private final HabitsRepository habitsRepository;

    @Transactional
    @Override
    public void deleteHabit(Long id, Long userId) {
        HabitEntity habitEntity = habitsRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));

        this.habitsRepository.delete(habitEntity);
    }

}
