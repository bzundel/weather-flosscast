package de.frauas.weather_flosscast
import kotlinx.datetime.*
import kotlin.random.Random

fun generateMockForecast(): Forecast {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.date

    val units = Units(
        temperature = "°C",
        humidity = "%",
        precipitationProbability = "%",
        rain = "mm",
        showers = "mm",
        snow = "cm"
    )

    val days = (0 until 7).map { dayOffset ->
        val date = today.plus(dayOffset, DateTimeUnit.DAY)
        val sunrise = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 6, 0)
        val sunset = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 21, 0)

        val hourlyValues = (0 until 24).map { hour ->
            val dateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, 0)
            val tempBase = 15 + Random.nextDouble(-5.0, 10.0)
            val isDaytime = hour in 6..18

            Hourly(
                dateTime = dateTime,
                temperature = if (isDaytime) tempBase + 5 else tempBase,
                relativeHumidity = Random.nextInt(40, 90),
                precipitationProbability = Random.nextInt(0, 60),
                rain = if (Random.nextBoolean()) Random.nextDouble(0.0, 2.0) else 0.0,
                showers = if (Random.nextBoolean()) Random.nextDouble(0.0, 1.5) else 0.0,
                snowfall = if (tempBase < 2) Random.nextDouble(0.0, 1.0) else 0.0,
                weatherCode = Random.nextInt(0, 100)
            )
        }

        DailyForecast(
            date = date,
            hourlyValues = hourlyValues,
            sunrise = sunrise,
            sunset = sunset
        )
    }

    return Forecast(
        timestamp = now,
        days = days,
        units = units
    )
}



