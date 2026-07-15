package com.example.Habits.business;

import com.example.Habits.business.Impl.HabitCompletion.UncompleteHabitImpl;
import com.example.Habits.business.streak.StreakCalculator;
import com.example.Habits.business.streak.StreakResult;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.exception.HabitNotCompletedException;
import com.example.Habits.exception.HabitNotFoundException;
import com.example.Habits.repository.HabitCompletionEntity;
import com.example.Habits.repository.HabitCompletionRepository;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UncompletedHabitImplTest {

    @Mock
    private HabitsRepository habitsRepository;

    @Mock
    private HabitCompletionRepository completionRepository;


    private StreakCalculator streakCalculator;
    @InjectMocks
    private UncompleteHabitImpl uncompleteHabit;


    @BeforeEach
    void setUp() {
        streakCalculator = new StreakCalculator();

        uncompleteHabit = new UncompleteHabitImpl(
                habitsRepository,
                completionRepository,
                streakCalculator
        );
    }

    private HabitEntity createHabit(Long habitId, Long userId, HabitFrequency frequency, int currentStreak, int bestStreak) {
        return HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(frequency)
                .habitCategory(HabitCategory.LEARNING)
                .currentStreak(currentStreak)
                .bestStreak(bestStreak)
                .active(true)
                .build();
    }

    private HabitCompletionEntity createCompletion(Long habitId, LocalDate completionDate) {
        return HabitCompletionEntity.builder()
                .habitId(habitId)
                .completionDate(completionDate)
                .build();
    }

    @Test
    void uncompleteHabit_shouldDeleteCurrentDailyCompletionAndRecalculateStreaks() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.DAILY, 3, 5);

        HabitCompletionEntity currentCompletion = HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        List<HabitCompletionEntity> remainingCompletions = List.of(HabitCompletionEntity.builder()
                        .habitId(habitId)
                        .completionDate(today.minusDays(2))
                        .build(),
                HabitCompletionEntity.builder()
                        .habitId(habitId)
                        .completionDate(today.minusDays(1))
                        .build()
        );

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdAndCompletionDate(habitId, today)).thenReturn(Optional.of(currentCompletion));

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(remainingCompletions);

        // Act
        uncompleteHabit.uncompleteHabit(habitId, userId);

        // Assert
        assertThat(habit.getCurrentStreak()).isEqualTo(2);
        assertThat(habit.getBestStreak()).isEqualTo(2);

        verify(completionRepository).delete(currentCompletion);
        verify(completionRepository).flush();

        verify(completionRepository).findAllByHabitIdOrderByCompletionDateAsc(habitId);
    }


    @Test
    void uncompleteHabit_shouldDeleteCurrentWeeklyCompletionAndRecalculateStreaks() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.WEEKLY, 3, 5);

        HabitCompletionEntity currentCompletion = HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        List<HabitCompletionEntity> remainingCompletions = List.of(createCompletion(habitId, weekStart.minusWeeks(2).plusDays(1)), createCompletion(habitId, weekStart.minusWeeks(1).plusDays(2))
        );

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(completionRepository.findFirstByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd)).thenReturn(Optional.of(currentCompletion));

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(remainingCompletions);

        // Act
        uncompleteHabit.uncompleteHabit(habitId, userId);

        // Assert
        assertThat(habit.getCurrentStreak()).isEqualTo(2);
        assertThat(habit.getBestStreak()).isEqualTo(2);

        verify(completionRepository).delete(currentCompletion);
        verify(completionRepository).flush();
    }

    @Test
    void uncompleteHabit_shouldThrow_whenHabitIsNotFoundForUser() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> uncompleteHabit.uncompleteHabit(habitId, userId)).isInstanceOf(HabitNotFoundException.class);

        verifyNoInteractions(completionRepository);
    }

    @Test
    void uncompleteHabit_shouldThrow_whenDailyHabitIsNotCompletedToday() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.DAILY, 2, 5);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdAndCompletionDate(habitId, today)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> uncompleteHabit.uncompleteHabit(habitId, userId)).isInstanceOf(HabitNotCompletedException.class);

        verify(completionRepository, never()).delete(any(HabitCompletionEntity.class));

        verify(completionRepository, never()).flush();

        verify(completionRepository, never()).findAllByHabitIdOrderByCompletionDateAsc(anyLong());
    }

    @Test
    void uncompleteHabit_shouldThrow_whenWeeklyHabitIsNotCompletedThisWeek() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.WEEKLY, 2, 5);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(completionRepository.findFirstByHabitIdAndCompletionDateBetween(habitId, weekStart,weekEnd)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> uncompleteHabit.uncompleteHabit(habitId, userId)).isInstanceOf(HabitNotCompletedException.class);

        verify(completionRepository, never()).delete(any(HabitCompletionEntity.class));

        verify(completionRepository, never()).flush();

        verify(completionRepository, never()).findAllByHabitIdOrderByCompletionDateAsc(anyLong());
    }

    @Test
    void uncompleteHabit_shouldResetStreaks_whenNoCompletionsRemain() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.DAILY, 1, 1);

        HabitCompletionEntity completion = HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdAndCompletionDate(habitId, today)).thenReturn(Optional.of(completion));

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of());

        // Act
        uncompleteHabit.uncompleteHabit(habitId, userId);

        // Assert
        assertThat(habit.getCurrentStreak()).isZero();
        assertThat(habit.getBestStreak()).isZero();

        verify(completionRepository).delete(completion);
        verify(completionRepository).flush();
    }

    @Test
    void uncompleteHabit_shouldRecalculateBrokenDailyStreak() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(habitId, userId, HabitFrequency.DAILY, 4, 4);

        HabitCompletionEntity currentCompletion = HabitCompletionEntity.builder()
                        .id(100L)
                        .habitId(habitId)
                        .completionDate(today)
                        .build();

        List<HabitCompletionEntity> remainingCompletions = List.of(
                createCompletion(habitId, today.minusDays(4)),
                createCompletion(habitId, today.minusDays(3)),
                createCompletion(habitId, today.minusDays(1))
        );

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(completionRepository.findByHabitIdAndCompletionDate(habitId, today)).thenReturn(Optional.of(currentCompletion));

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(remainingCompletions);

        // Act
        uncompleteHabit.uncompleteHabit(habitId, userId);

        // Assert
        assertThat(habit.getCurrentStreak()).isEqualTo(1);
        assertThat(habit.getBestStreak()).isEqualTo(2);
    }




}
