package com.example.liftapp.calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

class CurrentWeekData(private val firstDayOfWeek: DayOfWeek) {
    val startDate: LocalDate
    val endDate: LocalDate
    val daysInWeek: List<LocalDate>

    constructor(locale: Locale = Locale.getDefault()) : this(
        WeekFields.of(locale).firstDayOfWeek
    )

    init {
        val currentDate = LocalDate.now()
        startDate = currentDate.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        endDate = startDate.plusDays(6)
        daysInWeek = (0..6).map { startDate.plusDays(it.toLong()) }
    }

    fun getFormattedStartDate(formatter: DateTimeFormatter): String = formatter.format(startDate)
    fun getFormattedEndDate(formatter: DateTimeFormatter): String = formatter.format(endDate)
    fun getFormattedDays(formatter: DateTimeFormatter): List<String> = daysInWeek.map { formatter.format(it) }

    override fun toString(): String {
        return "Current Week: $startDate to $endDate"
    }
}