package com.example.Habits.business.Impl;

import com.example.Habits.business.IDeleteHabit;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@AllArgsConstructor
@Service
public class DeleteHabitImpl implements IDeleteHabit {
    private final HabitsRepository habitsRepository;
    private final HabitCompletionRepository completionRepository;

    @Transactional
    @Override
    public void deleteHabit(Long id, Long userId) {
        HabitEntity habitEntity = habitsRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new HabitNotFoundException(id));

        this.completionRepository.deleteAllByHabitId(id);
        this.habitsRepository.delete(habitEntity);
    }

}
