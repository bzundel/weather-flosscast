package de.frauas.weather_flosscast

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class Forecast(
    val timestamp: LocalDateTime,
    val days: List<DailyForecast>,
    val units: Units
) {
    companion object {  //Default values when the list is empty --> eliminates errors
        fun empty(): Forecast = Forecast(
            timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            days = emptyList(),
            units = Units.default()
        )
    }
}

data class DailyForecast(
    val date: LocalDate,
    val hourlyValues: List<Hourly>,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime
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
) { //Default values when the list is empty
    companion object {
        fun default() = Units(
            temperature = "°C",
            humidity = "%",
            precipitationProbability = "%",
            rain = "mm",
            showers = "mm",
            snow = "cm"
        )
    }
}

@kotlinx.serialization.Serializable
data class City(
    val cityName: String,
    val state: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)