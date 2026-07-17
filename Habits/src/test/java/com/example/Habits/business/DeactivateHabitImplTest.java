package com.example.Habits.business;

import com.example.Habits.business.Impl.DeactivateHabitImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeactivateHabitImplTest {
    @Mock
    private HabitsRepository habitsRepository;

    @InjectMocks
    private DeactivateHabitImpl deactivateHabitImpl;

    @Test
    void deactivateHabit_shouldDeactivateActiveHabit() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habitEntity = createHabit(
                habitId,
                userId,
                true
        );

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        // Act
        deactivateHabitImpl.deactivateHabit(habitId, userId);

        // Assert
        assertThat(habitEntity.isActive()).isFalse();

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoMoreInteractions(habitsRepository);
    }

    @Test
    void deactivateHabit_shouldRemainInactive_whenHabitIsAlreadyInactive() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habitEntity = createHabit(
                habitId,
                userId,
                false
        );

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        // Act
        deactivateHabitImpl.deactivateHabit(habitId, userId);

        // Assert
        assertThat(habitEntity.isActive()).isFalse();

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoMoreInteractions(habitsRepository);
    }

    @Test
    void deactivateHabit_shouldKeepOtherHabitFieldsUnchanged() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .currentStreak(4)
                .bestStreak(7)
                .active(true)
                .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        // Act
        deactivateHabitImpl.deactivateHabit(habitId, userId);

        // Assert
        assertThat(habitEntity.isActive()).isFalse();
        assertThat(habitEntity.getId()).isEqualTo(habitId);
        assertThat(habitEntity.getUserId()).isEqualTo(userId);
        assertThat(habitEntity.getName()).isEqualTo("Read");
        assertThat(habitEntity.getHabitFrequency()).isEqualTo(HabitFrequency.DAILY);
        assertThat(habitEntity.getHabitCategory()).isEqualTo(HabitCategory.LEARNING);
        assertThat(habitEntity.getCurrentStreak()).isEqualTo(4);
        assertThat(habitEntity.getBestStreak()).isEqualTo(7);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoMoreInteractions(habitsRepository);
    }

    @Test
    void deactivateHabit_shouldThrow_whenHabitIsNotFoundForUser() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> deactivateHabitImpl.deactivateHabit(habitId, userId)).isInstanceOf(HabitNotFoundException.class);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoMoreInteractions(habitsRepository);
    }

    private HabitEntity createHabit(Long habitId, Long userId, boolean active) {
        return HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .currentStreak(0)
                .bestStreak(0)
                .active(active)
                .build();
    }
}
