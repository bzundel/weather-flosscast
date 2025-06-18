package de.frauas.weather_flosscast
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.util.Locale
import kotlin.random.Random

//Generates a mock forecast with random values
fun generateMockForecast(): Forecast {
    //gets current timestamp and day
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.date

    //Defines units used by flosscast application
    val units = Units(
        temperature = "°C",
        humidity = "%",
        precipitationProbability = "%",
        rain = "mm",
        showers = "mm",
        snow = "cm"
    )

    //Creates days from today till one week in the future, including sunrise and sunset at 6:00 and 21:00
    val days = (0 until 7).map { dayOffset ->
        val date = today.plus(dayOffset, DateTimeUnit.DAY)
        val sunrise = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 6, 0)
        val sunset = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 21, 0)

        //Creates 24 hourly values for every day
        val hourlyValues = (0 until 24).map { hour ->
            val dateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, 0)
            //Crates random temperature bases for each hour, which are used to calculate temperatures later
            val tempBase = 15 + Random.nextDouble(-5.0, 10.0)
            val isDaytime = hour in 6..18

            Hourly(
                dateTime = dateTime,
                //Temprature and rain/snow are calculated based on the earlier defined tempBase value
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

//Generates mock forecast with the same temperature for all fields
//For comments check generateMockForecast, because the functions are nearly identical
fun generateSpecifiedMockForecast(customTemperature: Double): Forecast {
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

            Hourly(
                dateTime = dateTime,
                temperature = customTemperature,
                relativeHumidity = Random.nextInt(40, 90),
                precipitationProbability = Random.nextInt(0, 60),
                rain = 0.0,
                showers = 0.0,
                snowfall = 0.0,
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

//Generates mock forecast with an expired timestamp (approximatly 3 hours behind the current time)
//For comments check generateMockForecast, because the functions are nearly identical
fun generateExpiredMockForecast(): Forecast {
    val now = Clock.System.now().minus(3, DateTimeUnit.HOUR).toLocalDateTime(TimeZone.currentSystemDefault())
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

//Jsonifies forecast and coordinates for easier creation of cache files
fun jsonifyForecastWithCoordinates(forecast : Forecast, latitude: Double, longitude: Double): JsonObject {
    //Empty Json object gets created
    val cacheForecastList = Json.parseToJsonElement(Json.encodeToString(JsonObject(emptyMap()))).jsonObject
    //Jsonifies forecast
    val forecastJson: JsonObject = serializeForecast(forecast)
    //Extends empty json object with jsonified forecast and coordinates with correct encoding
    val updatedCacheForecastList = JsonObject(cacheForecastList + ("${String.format(Locale.ENGLISH, "%.1f", latitude)}:${String.format(Locale.ENGLISH, "%.1f", longitude)}" to forecastJson))
    //Returns the created Jsonobject
    return updatedCacheForecastList
}