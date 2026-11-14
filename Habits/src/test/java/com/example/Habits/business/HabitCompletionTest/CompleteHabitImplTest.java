package com.example.Habits.business.HabitCompletionTest;

import com.example.Habits.business.Impl.HabitCompletion.CompleteHabitImpl;
import com.example.Habits.business.streak.StreakCalculator;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompleteHabitImplTest {
    @Mock
    private HabitsRepository habitsRepository;
    @Mock
    private HabitCompletionRepository completionRepository;
    @Spy
    private StreakCalculator streakCalculator = new StreakCalculator();
    @InjectMocks
    private CompleteHabitImpl completeHabit;

    private HabitEntity createHabit(Long habitId, Long userId, HabitFrequency frequency, boolean active) {
        return HabitEntity.builder()
                .id(habitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(frequency)
                .habitCategory(HabitCategory.LEARNING)
                .currentStreak(0)
                .bestStreak(0)
                .active(active)
                .build();
    }

    private HabitCompletionEntity createCompletion(Long id, Long habitId, LocalDate date) {
        return HabitCompletionEntity.builder()
                .id(id)
                .habitId(habitId)
                .completionDate(date)
                .build();
    }

    @Test
    void completeHabit_shouldCreateFirstDailyCompletionAndStartStreak() {
        Long habitId = 1L;
        Long userId = 10L;
        Long completionId = 100L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);

        HabitCompletionEntity savedCompletion = createCompletion(completionId, habitId, today);

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(false);

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(savedCompletion));

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

        verify(completionRepository).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldIncrementDailyStreak_whenPreviousCompletionWasYesterday() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        habitEntity.setCurrentStreak(3);
        habitEntity.setBestStreak(5);

        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, today);

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(false);

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(
                createCompletion(87L, habitId, today.minusDays(10)),
                createCompletion(88L, habitId, today.minusDays(9)),
                createCompletion(89L, habitId, today.minusDays(8)),
                createCompletion(90L, habitId, today.minusDays(7)),
                createCompletion(91L, habitId, today.minusDays(6)),
                createCompletion(92L, habitId, today.minusDays(2)),
                createCompletion(93L, habitId, today.minusDays(1)),
                savedCompletion
        ));

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        // Assert
        assertThat(response.getCurrentStreak()).isEqualTo(3);
        assertThat(response.getBestStreak()).isEqualTo(5);

        assertThat(habitEntity.getCurrentStreak()).isEqualTo(3);
        assertThat(habitEntity.getBestStreak()).isEqualTo(5);

        verify(completionRepository).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldUpdateBestStreak_whenCurrentStreakExceedsBest() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        habitEntity.setCurrentStreak(4);
        habitEntity.setBestStreak(4);

        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, today);

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(false);

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(
                createCompletion(1L, habitId, today.minusDays(4)),
                createCompletion(2L, habitId, today.minusDays(3)),
                createCompletion(3L, habitId, today.minusDays(2)),
                createCompletion(4L, habitId, today.minusDays(1)),
                savedCompletion
        ));

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

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        habitEntity.setCurrentStreak(6);
        habitEntity.setBestStreak(10);

        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, today);

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(false);

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(
                createCompletion(1L, habitId, today.minusDays(3)),
                savedCompletion
        ));

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        // Assert
        assertThat(response.getCurrentStreak()).isEqualTo(1);
        assertThat(response.getBestStreak()).isEqualTo(1);
    }

    @Test
    void completeHabit_shouldThrow_whenDailyHabitAlreadyCompletedToday() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId)).isInstanceOf(HabitAlreadyCompletedException.class);

        verify(completionRepository, Mockito.never()).findAllByHabitIdOrderByCompletionDateAsc(anyLong());

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldThrow_whenHabitIsInactive() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, false);

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.of(habitEntity));

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

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.WEEKLY, true);

        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, today);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd)).thenReturn(false);

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(
                createCompletion(1L, habitId, weekStart.minusWeeks(2).plusDays(1)),
                createCompletion(2L, habitId, weekStart.minusWeeks(1).plusDays(2)),
                savedCompletion
        ));

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId);

        // Assert
        assertThat(response.getCurrentStreak()).isEqualTo(3);
        assertThat(response.getBestStreak()).isEqualTo(3);
    }

    @Test
    void completeHabit_shouldThrow_whenWeeklyHabitAlreadyCompletedThisWeek() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.WEEKLY, true);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId)).isInstanceOf(HabitAlreadyCompletedException.class);

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    // ---- backfill ----

    @Test
    void completeHabit_shouldBackfillPastDate_andRecomputeStreaksFromAllCompletions() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();
        LocalDate backfillDate = today.minusDays(2);

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        habitEntity.setCreatedAt(today.minusDays(30).atStartOfDay());
        habitEntity.setCurrentStreak(2);
        habitEntity.setBestStreak(2);

        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, backfillDate);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, backfillDate)).thenReturn(false);

        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);

        // yesterday and today were done already; filling the gap joins them into a run of 3
        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(
                savedCompletion,
                createCompletion(101L, habitId, today.minusDays(1)),
                createCompletion(102L, habitId, today)
        ));

        // Act
        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId, backfillDate);

        // Assert
        assertThat(response.getCompletionDate()).isEqualTo(backfillDate);
        assertThat(response.getCurrentStreak()).isEqualTo(3);
        assertThat(response.getBestStreak()).isEqualTo(3);
        assertThat(habitEntity.getCurrentStreak()).isEqualTo(3);
        assertThat(habitEntity.getBestStreak()).isEqualTo(3);

        ArgumentCaptor<HabitCompletionEntity> captor = ArgumentCaptor.forClass(HabitCompletionEntity.class);
        verify(completionRepository).save(captor.capture());
        assertThat(captor.getValue().getCompletionDate()).isEqualTo(backfillDate);
    }

    @Test
    void completeHabit_shouldUseToday_whenDateIsNull() {
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, today);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));
        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, today)).thenReturn(false);
        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);
        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(savedCompletion));

        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId, null);

        assertThat(response.getCompletionDate()).isEqualTo(today);
    }

    @Test
    void completeHabit_shouldThrowIllegalArgument_whenDateIsInTheFuture() {
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId, LocalDate.now().plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldThrowIllegalArgument_whenDateIsBeforeHabitCreation() {
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        habitEntity.setCreatedAt(today.minusDays(5).atTime(9, 30));

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId, today.minusDays(6)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before the habit was created");

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldAllowDateOnCreationDay() {
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();
        LocalDate creationDay = today.minusDays(5);

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        habitEntity.setCreatedAt(LocalDateTime.of(creationDay, java.time.LocalTime.of(18, 0)));
        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, creationDay);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));
        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, creationDay)).thenReturn(false);
        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);
        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(savedCompletion));

        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId, creationDay);

        assertThat(response.getCompletionDate()).isEqualTo(creationDay);
        assertThat(response.getCurrentStreak()).isZero();
        assertThat(response.getBestStreak()).isEqualTo(1);
    }

    @Test
    void completeHabit_shouldThrowInactive_whenBackfillingPausedHabit() {
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, false);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));

        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId, LocalDate.now().minusDays(1)))
                .isInstanceOf(InactiveHabitException.class);

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldThrowAlreadyCompleted_whenBackfillDateAlreadyDone() {
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate date = LocalDate.now().minusDays(3);

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));
        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, date)).thenReturn(true);

        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId, date))
                .isInstanceOf(HabitAlreadyCompletedException.class);

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldApplyWeeklyRuleToTheWeekOfTheBackfillDate() {
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate date = LocalDate.now().minusWeeks(2);
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.WEEKLY, true);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));
        when(completionRepository.existsByHabitIdAndCompletionDateBetween(habitId, weekStart, weekEnd)).thenReturn(true);

        assertThatThrownBy(() -> completeHabit.completeHabit(habitId, userId, date))
                .isInstanceOf(HabitAlreadyCompletedException.class);

        verify(completionRepository, Mockito.never()).save(ArgumentMatchers.<HabitCompletionEntity>any());
    }

    @Test
    void completeHabit_shouldBackfillWeeklyHabit_whenThatWeekIsFree() {
        Long habitId = 1L;
        Long userId = 10L;
        LocalDate today = LocalDate.now();
        LocalDate date = today.minusWeeks(1);
        LocalDate thisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.WEEKLY, true);
        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, date);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));
        when(completionRepository.existsByHabitIdAndCompletionDateBetween(habitId, weekStart, weekStart.plusDays(6))).thenReturn(false);
        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);
        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(
                savedCompletion,
                createCompletion(101L, habitId, thisWeek)
        ));

        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId, date);

        assertThat(response.getCurrentStreak()).isEqualTo(2);
        assertThat(response.getBestStreak()).isEqualTo(2);
    }

    @Test
    void completeHabit_shouldRecomputeStreaksUsingTheHabitWeekdays() {
        Long habitId = 1L;
        Long userId = 10L;
        // Dates are built relative to the most recent scheduled day (Monday or Wednesday).
        LocalDate today = LocalDate.now();
        LocalDate lastScheduled = today;
        while (lastScheduled.getDayOfWeek() != DayOfWeek.WEDNESDAY && lastScheduled.getDayOfWeek() != DayOfWeek.MONDAY) {
            lastScheduled = lastScheduled.minusDays(1);
        }
        LocalDate previousScheduled = lastScheduled.getDayOfWeek() == DayOfWeek.WEDNESDAY
                ? lastScheduled.minusDays(2)
                : lastScheduled.minusDays(5);

        HabitEntity habitEntity = createHabit(habitId, userId, HabitFrequency.DAILY, true);
        habitEntity.setWeekdays(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        HabitCompletionEntity savedCompletion = createCompletion(100L, habitId, lastScheduled);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habitEntity));
        when(completionRepository.existsByHabitIdAndCompletionDate(habitId, lastScheduled)).thenReturn(false);
        when(completionRepository.save(ArgumentMatchers.<HabitCompletionEntity>any())).thenReturn(savedCompletion);
        when(completionRepository.findAllByHabitIdOrderByCompletionDateAsc(habitId)).thenReturn(List.of(
                createCompletion(99L, habitId, previousScheduled),
                savedCompletion
        ));

        CompleteHabitResponse response = completeHabit.completeHabit(habitId, userId, lastScheduled);

        // two consecutive scheduled days, even though they are 2 or 5 calendar days apart
        assertThat(response.getBestStreak()).isEqualTo(2);
    }
}
