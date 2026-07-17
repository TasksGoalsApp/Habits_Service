package com.example.Habits.business;

import com.example.Habits.repository.HabitsRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteHabitImplTest {
    @Mock
    private HabitsRepository habitsRepository;
}
