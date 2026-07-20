package com.example.Habits.business;

import com.example.Habits.business.Impl.DeleteHabitImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteHabitImplTest {
    @Mock
    private HabitsRepository habitsRepository;

    @Mock
    private HabitCompletionRepository completionRepository;

    @InjectMocks
    private DeleteHabitImpl deleteHabitImpl;

    @Test
    void deleteHabit_shouldDeleteCompletionsAndHabit() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habit = createHabit(habitId, userId);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        // Act
        deleteHabitImpl.deleteHabit(habitId, userId);

        // Assert
        verify(habitsRepository).findByIdAndUserId(habitId, userId);

        verify(completionRepository).deleteAllByHabitId(habitId);

        verify(habitsRepository).delete(habit);

        verifyNoMoreInteractions(habitsRepository, completionRepository);
    }

    @Test
    void deleteHabit_shouldDeleteCompletionsBeforeDeletingHabit() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habit = createHabit(habitId, userId);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        // Act
        deleteHabitImpl.deleteHabit(habitId, userId);

        // Assert
        InOrder inOrder = inOrder(habitsRepository, completionRepository);

        inOrder.verify(habitsRepository).findByIdAndUserId(habitId, userId);

        inOrder.verify(completionRepository).deleteAllByHabitId(habitId);

        inOrder.verify(habitsRepository).delete(habit);
    }

    @Test
    void deleteHabit_shouldThrow_whenHabitIsNotFoundForUser() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> deleteHabitImpl.deleteHabit(habitId, userId)).isInstanceOf(HabitNotFoundException.class);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);

        verifyNoInteractions(completionRepository);

        verify(habitsRepository, never()).delete(any(HabitEntity.class));
    }

    private HabitEntity createHabit(Long habitId, Long userId) {
        return HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .currentStreak(0)
                .bestStreak(0)
                .active(true)
                .build();
    }
}
