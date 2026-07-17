package com.example.Habits.business;

import com.example.Habits.business.Impl.CreateHabitImpl;
import com.example.Habits.domain.HabitCategory;
import com.example.Habits.domain.HabitFrequency;
import com.example.Habits.domain.Request.CreateHabitRequest;
import com.example.Habits.domain.Response.CreateHabitResponse;
import com.example.Habits.repository.HabitEntity;
import com.example.Habits.repository.HabitsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHabitImplTest {
    @Mock
    private HabitsRepository repository;

    @InjectMocks
    private CreateHabitImpl createHabitImpl;

    @Test
    void createHabit_shouldSaveHabitAndReturnGeneratedId() {
        // Arrange
        Long userId = 10L;
        Long generatedHabitId = 100L;

        CreateHabitRequest request = CreateHabitRequest.builder()
                .name("Read")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .build();

        HabitEntity savedHabit = HabitEntity.builder()
                .id(generatedHabitId)
                .userId(userId)
                .name("Read")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .build();

        when(repository.save(ArgumentMatchers.<HabitEntity>any())).thenReturn(savedHabit);

        // Act
        CreateHabitResponse response = createHabitImpl.createHabit(request, userId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(generatedHabitId);

        verify(repository).save(ArgumentMatchers.<HabitEntity>any());
    }

    @Test
    void createHabit_shouldMapRequestAndUserIdToEntity() {
        // Arrange
        Long userId = 10L;

        CreateHabitRequest request = CreateHabitRequest.builder()
                .name("Drink water")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.HEALTH)
                .build();

        when(repository.save(ArgumentMatchers.<HabitEntity>any())).thenAnswer(invocation -> {
            HabitEntity entity = invocation.getArgument(0, HabitEntity.class);
            entity.setId(100L);
            return entity;
        });

        // Act
        createHabitImpl.createHabit(request, userId);

        // Assert
        ArgumentCaptor<HabitEntity> captor = ArgumentCaptor.forClass(HabitEntity.class);

        verify(repository).save(captor.capture());

        HabitEntity savedEntity = captor.getValue();

        assertThat(savedEntity.getUserId()).isEqualTo(userId);
        assertThat(savedEntity.getName()).isEqualTo("Drink water");
        assertThat(savedEntity.getHabitFrequency()).isEqualTo(HabitFrequency.DAILY);
        assertThat(savedEntity.getHabitCategory()).isEqualTo(HabitCategory.HEALTH);
    }

    @Test
    void createHabit_shouldTrimHabitNameBeforeSaving() {
        // Arrange
        Long userId = 10L;

        CreateHabitRequest request = CreateHabitRequest.builder()
                .name("   Read every day   ")
                .habitFrequency(HabitFrequency.DAILY)
                .habitCategory(HabitCategory.LEARNING)
                .build();

        when(repository.save(ArgumentMatchers.<HabitEntity>any())).thenAnswer(invocation -> {
            HabitEntity entity = invocation.getArgument(0, HabitEntity.class);
            entity.setId(100L);
            return entity;
        });

        // Act
        createHabitImpl.createHabit(request, userId);

        // Assert
        ArgumentCaptor<HabitEntity> captor = ArgumentCaptor.forClass(HabitEntity.class);

        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Read every day");
    }

    @Test
    void createHabit_shouldUseInitialHabitDefaults() {
        // Arrange
        Long userId = 10L;

        CreateHabitRequest request = CreateHabitRequest.builder()
                .name("Exercise")
                .habitFrequency(HabitFrequency.WEEKLY)
                .habitCategory(HabitCategory.FITNESS)
                .build();

        when(repository.save(ArgumentMatchers.<HabitEntity>any())).thenAnswer(invocation -> {
            HabitEntity entity = invocation.getArgument(0, HabitEntity.class);
            entity.setId(100L);
            return entity;
        });

        // Act
        createHabitImpl.createHabit(request, userId);

        // Assert
        ArgumentCaptor<HabitEntity> captor = ArgumentCaptor.forClass(HabitEntity.class);

        verify(repository).save(captor.capture());

        HabitEntity savedEntity = captor.getValue();

        assertThat(savedEntity.getCurrentStreak()).isZero();
        assertThat(savedEntity.getBestStreak()).isZero();
        assertThat(savedEntity.isActive()).isTrue();
    }
}

