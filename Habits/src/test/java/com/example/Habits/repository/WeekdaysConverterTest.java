package com.example.Habits.repository;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class WeekdaysConverterTest {
    private final WeekdaysConverter converter = new WeekdaysConverter();

    @Test
    void convertToDatabaseColumn_shouldWriteMondayFirstCommaSeparatedNames() {
        String column = converter.convertToDatabaseColumn(Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        assertThat(column).isEqualTo("MONDAY,WEDNESDAY,FRIDAY");
    }

    @Test
    void convertToDatabaseColumn_shouldWriteEveryDay_whenNullOrEmpty() {
        String everyDay = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY";

        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo(everyDay);
        assertThat(converter.convertToDatabaseColumn(Set.of())).isEqualTo(everyDay);
    }

    @Test
    void convertToEntityAttribute_shouldReadNames() {
        assertThat(converter.convertToEntityAttribute("TUESDAY, SUNDAY"))
                .isEqualTo(EnumSet.of(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY));
    }

    @Test
    void convertToEntityAttribute_shouldReturnEveryDay_forNullOrBlankColumns() {
        assertThat(converter.convertToEntityAttribute(null)).hasSize(7);
        assertThat(converter.convertToEntityAttribute("  ")).hasSize(7);
    }
}
