package com.example.Habits.business;

import com.example.Habits.business.Impl.HabitCompletion.GetHabitCompletionStatusImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Response.HabitCompletion.HabitCompletionStatusResponse;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitCompletionEntity;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GetHabitCompletionStatusImplTest {
    @Mock
    private HabitsRepository habitsRepository;

    @Mock
    private HabitCompletionRepository habitCompletionRepository;

    @InjectMocks
    private GetHabitCompletionStatusImpl getHabitCompletionStatusImpl;

    @Test
    void getStatus_shouldReturnCompletedStatus_whenDailyHabitIsCompletedToday() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.DAILY);

        HabitCompletionEntity completion = createCompletion(habitId, today);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(habitCompletionRepository.findByHabitIdAndCompletionDate(habitId, today)).thenReturn(Optional.of(completion));

        // Act
        HabitCompletionStatusResponse response = getHabitCompletionStatusImpl.getStatus(habitId, userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getHabitId()).isEqualTo(habitId);
        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getPeriodStart()).isEqualTo(today);
        assertThat(response.getPeriodEnd()).isEqualTo(today);
        assertThat(response.getCompletionDate()).isEqualTo(today);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verify(habitCompletionRepository).findByHabitIdAndCompletionDate(habitId, today);

        verifyNoMoreInteractions(habitsRepository, habitCompletionRepository);
    }

    @Test
    void getStatus_shouldReturnNotCompletedStatus_whenDailyHabitIsNotCompletedToday() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.DAILY);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(habitCompletionRepository.findByHabitIdAndCompletionDate(habitId, today)).thenReturn(Optional.empty());

        // Act
        HabitCompletionStatusResponse response = getHabitCompletionStatusImpl.getStatus(habitId, userId);

        // Assert
        assertThat(response.getHabitId()).isEqualTo(habitId);
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.getPeriodStart()).isEqualTo(today);
        assertThat(response.getPeriodEnd()).isEqualTo(today);
        assertThat(response.getCompletionDate()).isNull();

        verify(habitCompletionRepository).findByHabitIdAndCompletionDate(habitId, today);
    }

    @Test
    void getStatus_shouldReturnCompletedStatus_whenWeeklyHabitIsCompletedThisWeek() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate completionDate = weekStart.plusDays(2);

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.WEEKLY);

        HabitCompletionEntity completion = createCompletion(habitId, completionDate);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(habitCompletionRepository.findFirstByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd)).thenReturn(Optional.of(completion));

        // Act
        HabitCompletionStatusResponse response = getHabitCompletionStatusImpl.getStatus(habitId, userId);

        // Assert
        assertThat(response.getHabitId()).isEqualTo(habitId);
        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getPeriodStart()).isEqualTo(weekStart);
        assertThat(response.getPeriodEnd()).isEqualTo(weekEnd);
        assertThat(response.getCompletionDate()).isEqualTo(completionDate);

        verify(habitCompletionRepository).findFirstByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd);

        verify(habitCompletionRepository, never()).findByHabitIdAndCompletionDate(anyLong(), any(LocalDate.class));
    }

    @Test
    void getStatus_shouldReturnNotCompletedStatus_whenWeeklyHabitIsNotCompletedThisWeek() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.WEEKLY);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(habitCompletionRepository.findFirstByHabitIdAndCompletionDateBetween(habitId, weekStart,weekEnd)).thenReturn(Optional.empty());

        // Act
        HabitCompletionStatusResponse response = getHabitCompletionStatusImpl.getStatus(habitId, userId);

        // Assert
        assertThat(response.getHabitId()).isEqualTo(habitId);
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.getPeriodStart()).isEqualTo(weekStart);
        assertThat(response.getPeriodEnd()).isEqualTo(weekEnd);
        assertThat(response.getCompletionDate()).isNull();

        verify(habitCompletionRepository).findFirstByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd);
    }

    @Test
    void getStatus_shouldThrow_whenHabitIsNotFoundForUser() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> getHabitCompletionStatusImpl.getStatus(habitId, userId)).isInstanceOf(HabitNotFoundException.class);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoInteractions(habitCompletionRepository);
    }

    private HabitEntity createHabit(Long habitId, Long userId, HabitFrequency frequency) {
        return HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(frequency)
                .habitCategory(HabitCategory.LEARNING)
                .currentStreak(0)
                .bestStreak(0)
                .active(true)
                .build();
    }

    private HabitCompletionEntity createCompletion(Long habitId, LocalDate completionDate) {
        return HabitCompletionEntity.builder()
                .id(100L)
                .habitId(habitId)
                .completionDate(completionDate)
                .build();
    }

}
