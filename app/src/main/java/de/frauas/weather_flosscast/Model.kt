package de.frauas.weather_flosscast

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class Forecast(
    val days: List<DailyForecast>,
    val unit: String
)

data class DailyForecast(
    val date: LocalDate,
    val temperatures: List<TemperatureItem>
)

data class TemperatureItem(
    val dateTime: LocalDateTime,
    val temperature: Double
)

