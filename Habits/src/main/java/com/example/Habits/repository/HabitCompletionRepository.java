package com.example.Habits.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletionEntity, Long> {

    boolean existsByHabitIdAndCompletionDate(Long habitId, LocalDate completionDate);
    boolean existsByHabitIdAndCompletionDateBetween(Long habitId, LocalDate startDate, LocalDate endDate);
    Optional<HabitCompletionEntity> findTopByHabitIdOrderByCompletionDateDesc(Long habitId);

    List<HabitCompletionEntity> findAllByHabitIdOrderByCompletionDateDesc(Long habitId);

    void deleteAllByHabitId(Long habitId);

    void deleteAllByHabitIdIn(List<Long> habitIds);

}
