package de.frauas.weather_flosscast

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class Forecast(
    val timestamp: LocalDateTime,
    val days: List<DailyForecast>,
    val units: Units
)

data class DailyForecast(
    val date: LocalDate,
    val hourlyValues: List<Hourly>
)

data class Hourly(
    val dateTime: LocalDateTime,
    val temperature: Double,
    val relativeHumidity: Int,
    val precipitationProbability: Int,
    val rain: Double,
    val showers: Double,
    val snowfall: Double,
    val weatherCode: Int
)

data class Units(
    val temperature: String,
    val humidity: String,
    val precipitationProbability: String,
    val rain: String,
    val showers: String,
    val snow: String,
)
