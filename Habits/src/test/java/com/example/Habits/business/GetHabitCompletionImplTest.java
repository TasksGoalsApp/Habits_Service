package com.example.Habits.business;
import com.example.Habits.business.Impl.HabitCompletion.GetHabitCompletionsImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Response.HabitCompletion.GetHabitCompletionResponse;
import com.example.Habits.domain.Response.HabitCompletion.HabitCompletionResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class GetHabitCompletionImplTest {
    @Mock
    private HabitsRepository habitsRepository;


    @Mock
    private HabitCompletionRepository habitCompletionRepository;

    @InjectMocks
    private GetHabitCompletionsImpl getHabitCompletionsImpl;

    @Test
    void getCompletions_shouldReturnMappedCompletions() {
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habit = createHabit(habitId, userId);

        HabitCompletionEntity newestCompletion = createCompletion(
                101L,
                habitId,
                LocalDate.of(2026, 7, 20)
        );

        HabitCompletionEntity olderCompletion = createCompletion(
                100L,
                habitId,
                LocalDate.of(2026, 7, 19)
        );

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(habitCompletionRepository.findAllByHabitIdOrderByCompletionDateDesc(habitId)).thenReturn(List.of(
                newestCompletion,
                olderCompletion
        ));

        GetHabitCompletionResponse response = getHabitCompletionsImpl.getCompletions(habitId, userId);

        assertThat(response).isNotNull();
        assertThat(response.getHabitId()).isEqualTo(habitId);

        // This must be hasSize(), not isEqualTo(2)
        assertThat(response.getCompletions()).hasSize(2);

        assertThat(response.getCompletions().get(0).getId()).isEqualTo(101L);

        assertThat(response.getCompletions().get(0).getCompletionDate()).isEqualTo(LocalDate.of(2026, 7, 20));

        assertThat(response.getCompletions().get(1).getId()).isEqualTo(100L);

        assertThat(response.getCompletions().get(1).getCompletionDate()).isEqualTo(LocalDate.of(2026, 7, 19));

        verify(habitsRepository).findByIdAndUserId(habitId, userId);

        verify(habitCompletionRepository).findAllByHabitIdOrderByCompletionDateDesc(habitId);

        verifyNoMoreInteractions(habitsRepository, habitCompletionRepository);
    }

    @Test
    void getCompletions_shouldReturnEmptyList_whenHabitHasNoCompletions() {
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habit = createHabit(habitId, userId);

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(habitCompletionRepository.findAllByHabitIdOrderByCompletionDateDesc(habitId)).thenReturn(List.of());

        GetHabitCompletionResponse response = getHabitCompletionsImpl.getCompletions(habitId, userId);

        assertThat(response).isNotNull();
        assertThat(response.getHabitId()).isEqualTo(habitId);
        assertThat(response.getCompletions()).isNotNull();
        assertThat(response.getCompletions()).isEmpty();

        verify(habitsRepository).findByIdAndUserId(habitId, userId);

        verify(habitCompletionRepository).findAllByHabitIdOrderByCompletionDateDesc(habitId);

        verifyNoMoreInteractions(habitsRepository, habitCompletionRepository);
    }

    @Test
    void getCompletions_shouldPreserveRepositoryOrdering() {
        Long habitId = 1L;
        Long userId = 10L;

        HabitEntity habit = createHabit(habitId, userId);

        List<HabitCompletionEntity> repositoryResult = List.of(
                createCompletion(
                        103L,
                        habitId,
                        LocalDate.of(2026, 7, 20)
                ),
                createCompletion(
                        102L,
                        habitId,
                        LocalDate.of(2026, 7, 15)
                ),
                createCompletion(
                        101L,
                        habitId,
                        LocalDate.of(2026, 7, 10)
                )
        );

        when(habitsRepository.findByIdAndUserId(habitId, userId)).thenReturn(Optional.of(habit));

        when(habitCompletionRepository.findAllByHabitIdOrderByCompletionDateDesc(habitId)).thenReturn(repositoryResult);

        GetHabitCompletionResponse response = getHabitCompletionsImpl.getCompletions(habitId, userId);

        assertThat(response.getCompletions()).hasSize(3);

        assertThat(response.getCompletions()).extracting(HabitCompletionResponse::getCompletionDate)
                .containsExactly(
                        LocalDate.of(2026, 7, 20),
                        LocalDate.of(2026, 7, 15),
                        LocalDate.of(2026, 7, 10)
                );

        verify(habitsRepository).findByIdAndUserId(habitId, userId);

        verify(habitCompletionRepository).findAllByHabitIdOrderByCompletionDateDesc(habitId);
    }

    @Test
    void getCompletions_shouldThrow_whenHabitIsNotFoundForUser() {
        // Arrange
        Long habitId = 1L;
        Long userId = 10L;

        when(habitsRepository.findByIdAndUserId(habitId, userId))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> getHabitCompletionsImpl.getCompletions(habitId, userId)).isInstanceOf(HabitNotFoundException.class);

        verify(habitsRepository).findByIdAndUserId(habitId, userId);
        verifyNoInteractions(habitCompletionRepository);
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

    private HabitCompletionEntity createCompletion(Long completionId, Long habitId, LocalDate completionDate) {
        return HabitCompletionEntity.builder()
                .id(completionId)
                .habitId(habitId)
                .completionDate(completionDate)
                .build();
    }
}
