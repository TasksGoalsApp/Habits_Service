package com.example.Habits.business;

import com.example.Habits.business.Impl.HabitCompletion.CompleteHabitImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Response.HabitCompletion.CompleteHabitResponse;
import com.example.Habits.exception.HabitAlreadyCompletedException;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.exception.InactiveHabitException;
import com.example.Habits.repository.HabitCompletionEntity;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.Matchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.never;

@ExtendWith(MockitoExtension.class)
public class CompleteHabitImplTest {
    @Mock
    private HabitsRepository habitsRepository;
    @Mock
    private HabitCompletionRepository completionRepository;
    @InjectMocks
    private CompleteHabitImpl completeHabit;



    @Test
    void completeHabit_shouldCreateFirstDailyCompletionAndStartStreak() {
        Long habitId = 1L;
        Long userId = 10L;
        Long completionId = 100L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .currentStreak(0)
                .bestStreak(0)
                .active(true)
                .build();

        HabitCompletionEntity savedCompletion =
                HabitCompletionEntity.builder()
                        .id(completionId)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(false);

        when(completionRepository.findTopByHabitIdOrderByCompletionDateDesc(habitId)).thenReturn(Optional.empty());


        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        assertThat(response.getCompletionId()).isEqualTo(completionId);

        assertThat(response.getHabitId()).isEqualTo(habitId);

        assertThat(response.getCompletionDate()).isEqualTo(today);

        assertThat(response.getCurrentStreak()).isEqualTo(1);

        assertThat(response.getBestStreak()).isEqualTo(1);

        assertThat(habitEntity.getCurrentStreak()).isEqualTo(1);

        assertThat(habitEntity.getBestStreak()).isEqualTo(1);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);

        verify(completionRepository).existsByHabitIdAndCompletionDate(habitId, today);

        verify(completionRepository).findTopByHabitIdOrderByCompletionDateDesc(habitId);
        verify(completionRepository).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldIncrementDailyStreak_whenPreviousCompletionWasYesterday() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();


        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .habitFrequency(HabitFrequency.DAILY)
                .currentStreak(3)
                .bestStreak(5)
                .active(true)
                .build();

        HabitCompletionEntity previousCompletion =
                HabitCompletionEntity.builder()
                        .id(90L)
                        .habitId(habitId)
                        .completionDate(today.minusDays(1))
                        .build();

        HabitCompletionEntity savedCompletion =
                HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(
                habitId,
                today
        )).thenReturn(false);

        when(completionRepository
                .findTopByHabitIdOrderByCompletionDateDesc(habitId))
                .thenReturn(Optional.of(previousCompletion));

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        // Assert
        assertThat(response.getCurrentStreak()).isEqualTo(4);
        assertThat(response.getBestStreak()).isEqualTo(5);

        assertThat(habitEntity.getCurrentStreak()).isEqualTo(4);
        assertThat(habitEntity.getBestStreak()).isEqualTo(5);

        verify(completionRepository).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldUpdateBestStreak_whenCurrentStreakExceedsBest() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .habitFrequency(HabitFrequency.DAILY)
                .currentStreak(4)
                .bestStreak(4)
                .active(true)
                .build();

        HabitCompletionEntity previousCompletion =
                HabitCompletionEntity.builder()
                        .habitId(habitId)
                        .completionDate(today.minusDays(1))
                        .build();

        HabitCompletionEntity savedCompletion =
                HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(
                habitId,
                today
        )).thenReturn(false);

        when(completionRepository
                .findTopByHabitIdOrderByCompletionDateDesc(habitId))
                .thenReturn(Optional.of(previousCompletion));

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        // Assert
        assertThat(response.getCurrentStreak()).isEqualTo(5);
        assertThat(response.getBestStreak()).isEqualTo(5);
    }

    @Test
    void completeHabit_shouldStartNewStreak_whenPreviousDailyCompletionIsTooOld() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .habitFrequency(HabitFrequency.DAILY)
                .currentStreak(6)
                .bestStreak(10)
                .active(true)
                .build();


        HabitCompletionEntity previousCompletion =
                HabitCompletionEntity.builder()
                        .habitId(habitId)
                        .completionDate(today.minusDays(3))
                        .build();

        HabitCompletionEntity savedCompletion =
                HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(false);

        when(completionRepository.findTopByHabitIdOrderByCompletionDateDesc(habitId)).thenReturn(Optional.of(previousCompletion));

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        // Assert
        assertThat(response.getCurrentStreak()).isEqualTo(1);
        assertThat(response.getBestStreak()).isEqualTo(10);
    }

    @Test
    void completeHabit_shouldThrow_whenDailyHabitAlreadyCompletedToday() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .habitFrequency(HabitFrequency.DAILY)
                .currentStreak(3)
                .bestStreak(5)
                .active(true)
                .build();


        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(
                habitId,
                today
        )).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId)).isInstanceOf(HabitAlreadyCompletedException.class);

        verify(completionRepository,Mockito.never()).findTopByHabitIdOrderByCompletionDateDesc(anyLong());

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldThrow_whenHabitIsInactive() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .habitFrequency(HabitFrequency.DAILY)
                .currentStreak(0)
                .bestStreak(0)
                .active(false)
                .build();


        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(
                habitId,
                today
        )).thenReturn(false);

        when(completionRepository
                .findTopByHabitIdOrderByCompletionDateDesc(habitId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId)).isInstanceOf(InactiveHabitException.class);

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldThrow_whenHabitIsNotFoundForUser() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId)).isInstanceOf(HabitNotFoundException.class);

        verifyNoInteractions(completionRepository);
    }

    @Test
    void completeHabit_shouldIncrementWeeklyStreak_whenPreviousCompletionWasPreviousWeek() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .habitFrequency(HabitFrequency.WEEKLY)
                .currentStreak(2)
                .bestStreak(5)
                .active(true)
                .build();

        HabitCompletionEntity previousCompletion = HabitCompletionEntity.builder()
                        .habitId(habitId)
                        .completionDate(weekStart.minusWeeks(1).plusDays(2))
                        .build();

        HabitCompletionEntity savedCompletion = HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd)).thenReturn(false);

        when(completionRepository.findTopByHabitIdOrderByCompletionDateDesc(habitId)).thenReturn(Optional.of(previousCompletion));

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        // Assert
        assertThat(response.getCurrentStreak()).isEqualTo(3);
        assertThat(response.getBestStreak()).isEqualTo(5);
    }

    @Test
    void completeHabit_shouldThrow_whenWeeklyHabitAlreadyCompletedThisWeek() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habitEntity = HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .habitFrequency(HabitFrequency.WEEKLY)
                .currentStreak(2)
                .bestStreak(5)
                .active(true)
                .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId)).isInstanceOf(HabitAlreadyCompletedException.class);

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

}
