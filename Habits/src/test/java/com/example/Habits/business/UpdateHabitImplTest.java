package com.example.Habits.business;

import com.example.Habits.business.Impl.UpdateHabitImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Request.UpdateHabitRequest;
import com.example.Habits.domain.Response.UpdateHabitResponse;
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
public class UpdateHabitImplTest {
    @Mock
    private HabitsRepository habitsRepository;

    @InjectMocks
    private UpdateHabitImpl updateHabitImpl;

    @Test
    void updateHabit_shouldUpdateHabitFieldsAndReturnResponse() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity existingHabit = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Old habit")
                .habitCategory(HabitCategory.HEALTH)
                .habitFrequency(HabitFrequency.DAILY)
                .build();

        UpdateHabitRequest request = UpdateHabitRequest.builder()
                .name("Exercise")
                .habitFrequency(HabitFrequency.WEEKLY)
                .habitCategory(HabitCategory.FITNESS)
                .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(existingHabit));

        // Act
        UpdateHabitResponse response =
                updateHabitImpl.updateHabit(request, habitId, userId);

        // Assert
        assertThat(existingHabit.getName()).isEqualTo("Exercise");
        assertThat(existingHabit.getHabitFrequency())
                .isEqualTo(HabitFrequency.WEEKLY);
        assertThat(existingHabit.getHabitCategory())
                .isEqualTo(HabitCategory.FITNESS);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Exercise");
        assertThat(response.getHabitFrequency())
                .isEqualTo(HabitFrequency.WEEKLY);
        assertThat(response.getHabitCategory())
                .isEqualTo(HabitCategory.FITNESS);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoMoreInteractions(habitsRepository);
    }

    @Test
    void updateHabit_shouldTrimHabitName() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity existingHabit = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Old name")
                .habitCategory(HabitCategory.HEALTH)
                .habitFrequency(HabitFrequency.DAILY)
                .build();


        UpdateHabitRequest request = UpdateHabitRequest.builder()
                .name("   Read every day   ")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(existingHabit));

        // Act
        UpdateHabitResponse response = updateHabitImpl.updateHabit(request, habitId, userId);

        // Assert
        assertThat(existingHabit.getName()).isEqualTo("Read every day");
        assertThat(response.getName()).isEqualTo("Read every day");

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
    }

    @Test
    void updateHabit_shouldKeepNonEditableFieldsUnchanged() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity existingHabit = createHabit(
                habitId,
                userId,
                "Old habit",
                HabitFrequency.DAILY,
                HabitCategory.HEALTH
        );

        existingHabit.setCurrentStreak(4);
        existingHabit.setBestStreak(7);
        existingHabit.setActive(true);

        UpdateHabitRequest request = UpdateHabitRequest.builder()
                .name("Updated habit")
                .habitFrequency(HabitFrequency.WEEKLY)
                .habitCategory(HabitCategory.FITNESS)
                .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(existingHabit));

        // Act
        updateHabitImpl.updateHabit(request, habitId, userId);

        // Assert
        assertThat(existingHabit.getId()).isEqualTo(habitId);
        assertThat(existingHabit.getUserId()).isEqualTo(userId);
        assertThat(existingHabit.getCurrentStreak()).isEqualTo(4);
        assertThat(existingHabit.getBestStreak()).isEqualTo(7);
        assertThat(existingHabit.isActive()).isTrue();
    }

    @Test
    void updateHabit_shouldThrow_whenHabitIsNotFoundForUser() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        UpdateHabitRequest request = UpdateHabitRequest.builder()
                .name("Exercise")
                .habitFrequency(HabitFrequency.WEEKLY)
                .habitCategory(HabitCategory.FITNESS)
                .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                updateHabitImpl.updateHabit(request, habitId, userId)
        ).isInstanceOf(HabitNotFoundException.class);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoMoreInteractions(habitsRepository);
    }

    private HabitEntity createHabit(
            Long habitId,
            Long userId,
            String name,
            HabitFrequency frequency,
            HabitCategory category
    ) {
        return HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name(name)
                .habitFrequency(frequency)
                .habitCategory(category)
                .currentStreak(0)
                .bestStreak(0)
                .active(true)
                .build();
    }
}

