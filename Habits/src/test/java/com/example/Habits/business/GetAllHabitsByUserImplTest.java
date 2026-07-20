package com.example.Habits.business;

import com.example.Habits.business.Impl.GetAllHabitsByUserImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Response.GetAllHabitsByUserResponse;
import com.example.Habits.domain.Response.HabitResponse;
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
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GetAllHabitsByUserImplTest {
    @Mock
    private HabitsRepository habitsRepository;

    @Mock
    private HabitCompletionRepository completionRepository;

    @InjectMocks
    private GetAllHabitsByUserImpl getAllHabitsByUserImpl;

    @Test
    void getAllHabits_shouldReturnDailyHabitCompletedToday() {
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(
                1L,
                userId,
                "Read",
                HabitFrequency.DAILY,
                HabitCategory.LEARNING,
                4,
                8,
                true
        );

        when(habitsRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(habit));

        when(completionRepository.existsByHabitIdAndCompletionDate(habit.getId(), today)).thenReturn(true);

        GetAllHabitsByUserResponse response = getAllHabitsByUserImpl.getAllHabits(userId);

        assertThat(response).isNotNull();
        assertThat(response.getHabits()).hasSize(1);

        HabitResponse habitResponse = response.getHabits().get(0);

        assertThat(habitResponse.getId()).isEqualTo(1L);
        assertThat(habitResponse.getName()).isEqualTo("Read");
        assertThat(habitResponse.getHabitFrequency()).isEqualTo(HabitFrequency.DAILY);
        assertThat(habitResponse.getHabitCategory()).isEqualTo(HabitCategory.LEARNING);
        assertThat(habitResponse.getCurrentStreak()).isEqualTo(4);
        assertThat(habitResponse.getBestStreak()).isEqualTo(8);
        assertThat(habitResponse.isActive()).isTrue();
        assertThat(habitResponse.isCompletedForCurrentPeriod()).isTrue();

        verify(habitsRepository).findAllByUserIdOrderByCreatedAtDesc(userId);

        verify(completionRepository).existsByHabitIdAndCompletionDate(habit.getId(), today);

        verify(completionRepository, never()).existsByHabitIdAndCompletionDateBetween(
                        anyLong(),
                        any(LocalDate.class),
                        any(LocalDate.class)
                );
    }

    @Test
    void getAllHabits_shouldReturnDailyHabitNotCompletedToday() {
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        HabitEntity habit = createHabit(
                1L,
                userId,
                "Read",
                HabitFrequency.DAILY,
                HabitCategory.LEARNING,
                0,
                3,
                true
        );

        when(habitsRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(habit));

        when(completionRepository.existsByHabitIdAndCompletionDate(
                habit.getId(),
                today
        )).thenReturn(false);

        GetAllHabitsByUserResponse response = getAllHabitsByUserImpl.getAllHabits(userId);

        assertThat(response.getHabits()).hasSize(1);
        assertThat(response.getHabits().get(0).isCompletedForCurrentPeriod()).isFalse();

        verify(completionRepository).existsByHabitIdAndCompletionDate(habit.getId(), today);
    }

    @Test
    void getAllHabits_shouldReturnWeeklyHabitCompletedThisWeek() {
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habit = createHabit(
                2L,
                userId,
                "Exercise",
                HabitFrequency.WEEKLY,
                HabitCategory.HEALTH,
                2,
                5,
                true
        );

        when(habitsRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(habit));

        when(completionRepository.existsByHabitIdAndCompletionDateBetween(
                        habit.getId(),
                        weekStart,
                        weekEnd
                ))
                .thenReturn(true);

        GetAllHabitsByUserResponse response = getAllHabitsByUserImpl.getAllHabits(userId);

        assertThat(response.getHabits()).hasSize(1);

        HabitResponse habitResponse = response.getHabits().get(0);

        assertThat(habitResponse.getId()).isEqualTo(2L);
        assertThat(habitResponse.getHabitFrequency())
                .isEqualTo(HabitFrequency.WEEKLY);
        assertThat(habitResponse.isCompletedForCurrentPeriod()).isTrue();

        verify(completionRepository).existsByHabitIdAndCompletionDateBetween(
                        habit.getId(),
                        weekStart,
                        weekEnd
                );

        verify(completionRepository, never()).existsByHabitIdAndCompletionDate(
                        anyLong(),
                        any(LocalDate.class)
                );
    }

    @Test
    void getAllHabits_shouldReturnWeeklyHabitNotCompletedThisWeek() {
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity habit = createHabit(
                2L,
                userId,
                "Exercise",
                HabitFrequency.WEEKLY,
                HabitCategory.HEALTH,
                0,
                5,
                true
        );

        when(habitsRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(habit));

        when(completionRepository.existsByHabitIdAndCompletionDateBetween(
                        habit.getId(),
                        weekStart,
                        weekEnd
                ))
                .thenReturn(false);

        GetAllHabitsByUserResponse response = getAllHabitsByUserImpl.getAllHabits(userId);

        assertThat(response.getHabits()).hasSize(1);
        assertThat(response.getHabits().get(0).isCompletedForCurrentPeriod()).isFalse();

        verify(completionRepository).existsByHabitIdAndCompletionDateBetween(
                        habit.getId(),
                        weekStart,
                        weekEnd
                );
    }

    @Test
    void getAllHabits_shouldMapMultipleHabitsAndPreserveRepositoryOrder() {
        Long userId = 10L;
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        HabitEntity newestHabit = createHabit(
                3L,
                userId,
                "Exercise",
                HabitFrequency.WEEKLY,
                HabitCategory.HEALTH,
                2,
                6,
                true
        );

        HabitEntity olderHabit = createHabit(
                1L,
                userId,
                "Read",
                HabitFrequency.DAILY,
                HabitCategory.LEARNING,
                5,
                10,
                false
        );

        when(habitsRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(newestHabit, olderHabit));

        when(completionRepository.existsByHabitIdAndCompletionDateBetween(
                        newestHabit.getId(),
                        weekStart,
                        weekEnd
                ))
                .thenReturn(true);

        when(completionRepository.existsByHabitIdAndCompletionDate(
                        olderHabit.getId(),
                        today
                ))
                .thenReturn(false);

        GetAllHabitsByUserResponse response = getAllHabitsByUserImpl.getAllHabits(userId);

        assertThat(response.getHabits()).hasSize(2);

        assertThat(response.getHabits()).extracting(HabitResponse::getId).containsExactly(3L, 1L);

        assertThat(response.getHabits()).extracting(HabitResponse::getName).containsExactly("Exercise", "Read");

        assertThat(response.getHabits()).extracting(HabitResponse::isCompletedForCurrentPeriod).containsExactly(true, false);
        assertThat(response.getHabits().get(1).isActive()).isFalse();
    }

    @Test
    void getAllHabits_shouldReturnEmptyList_whenUserHasNoHabits() {
        Long userId = 10L;

        when(habitsRepository.findAllByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        GetAllHabitsByUserResponse response = getAllHabitsByUserImpl.getAllHabits(userId);

        assertThat(response).isNotNull();
        assertThat(response.getHabits()).isNotNull();
        assertThat(response.getHabits()).isEmpty();

        verify(habitsRepository).findAllByUserIdOrderByCreatedAtDesc(userId);

        verifyNoInteractions(completionRepository);
    }

    private HabitEntity createHabit(Long id, Long userId, String name, HabitFrequency frequency, HabitCategory category, int currentStreak, int bestStreak, boolean active) {
        return HabitEntity.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .habitFrequency(frequency)
                .habitCategory(category)
                .currentStreak(currentStreak)
                .bestStreak(bestStreak)
                .active(active)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
