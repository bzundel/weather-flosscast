package de.frauas.weather_flosscast

import android.net.http.HttpException
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import java.io.IOException

private val logger = KotlinLogging.logger { }

// convert double to string with two digits after the decimal point
private fun stripCoordinate(value: Double): String {
    return String.format(Locale.ENGLISH, "%.2f", value)
}

// build request url from latitude and longitude values
private fun buildUrl(latitude: Double, longitude: Double): String {
    return "https://api.open-meteo.com/v1/forecast?latitude=${stripCoordinate(latitude)}&longitude=${stripCoordinate(longitude)}&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,rain,showers,snowfall"
}

// make a call to the request url and return the raw response as a string
private fun getRawWeatherData(requestUrl: String): String {
    val url = URL(requestUrl)
    var connection: HttpURLConnection? = null

    try {
        connection = url.openConnection() as HttpURLConnection

        logger.debug { "Connection opened successfully" }

        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            return connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            logger.error { "Failed fetching data with response code ${connection.responseCode}" }

            throw IOException("Response code does not indicate success: ${connection.responseCode}")
        }
    } catch (e: IOException) {
        logger.error { "I/O Exception caught: ${e.message}" }

        throw IOException("Unable to fetch weather data", e)
    } finally {
        connection?.disconnect()

        logger.debug { "Connection closed" }
    }
}

// extension function to replace non-null assertion
private fun JsonObject.getOrThrow(key: String): JsonElement =
    this[key] ?: run {
        val message = "Key '${key} not found in JsonObject"
        logger.error { message }

        throw IllegalArgumentException(message)
    }

private fun convertToForecastObject(body: String): Forecast {
    val json: JsonObject = try {
        Json.parseToJsonElement(body).jsonObject
    } catch (e: SerializationException) {
        logger.error { "Could not parse received JSON. ${e.message}" }

        throw Exception("Could not parse to objects", e)
    }

    // extract units of hourly data
    val unitsJson: JsonObject = json["hourly_units"]!!.jsonObject
    val units = Units(
        unitsJson.getOrThrow("temperature_2m").jsonPrimitive.content,
        unitsJson.getOrThrow("relative_humidity_2m").jsonPrimitive.content,
        unitsJson.getOrThrow("precipitation_probability").jsonPrimitive.content,
        unitsJson.getOrThrow("rain").jsonPrimitive.content,
        unitsJson.getOrThrow("showers").jsonPrimitive.content,
        unitsJson.getOrThrow("snowfall").jsonPrimitive.content
    )

    logger.debug{ "Parsed units" }

    // extract hourly values and cast to respective type
    val hourly: JsonObject = json["hourly"]!!.jsonObject
    val hourlyTime =
        hourly.getOrThrow("time").jsonArray.map { LocalDateTime.parse(it.jsonPrimitive.content) }
    val hourlyTemperature =
        hourly.getOrThrow("temperature_2m").jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlyRelativeHumidity =
        hourly.getOrThrow("relative_humidity_2m").jsonArray.map { it.jsonPrimitive.content.toInt() }
    val hourlyPrecipitationProbability =
        hourly.getOrThrow("precipitation_probability").jsonArray.map { it.jsonPrimitive.content.toInt() }
    val hourlyRain = hourly.getOrThrow("rain").jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlyShowers =
        hourly.getOrThrow("showers").jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlySnowfall =
        hourly.getOrThrow("snowfall").jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlyWeatherCode =
        hourly.getOrThrow("weather_code").jsonArray.map { it.jsonPrimitive.content.toInt() }

    logger.debug { "Parsed hourly data" }

    // assert that all extracted lists have the same length
    try {
        check(
            listOf
                (
                hourlyTime,
                hourlyTemperature,
                hourlyRelativeHumidity,
                hourlyPrecipitationProbability,
                hourlyRain,
                hourlyShowers,
                hourlySnowfall,
                hourlyWeatherCode
            ).map { it.size }.toSet().size == 1
        ) { "Retrieved lists are of different sizes" }
    } catch (e: IllegalStateException) {
        logger.error { "Retrieved lists are of different sizes. Some magical API error? ${e.message}" }
    }

    logger.debug { "Passed list size assertion" }

    // map individual lists to single list of packed objects
    val hourlyValues: List<Hourly> = hourlyTime.indices.map {
        Hourly(
            dateTime = hourlyTime[it],
            temperature = hourlyTemperature[it],
            relativeHumidity = hourlyRelativeHumidity[it],
            precipitationProbability = hourlyPrecipitationProbability[it],
            rain = hourlyRain[it],
            showers = hourlyShowers[it],
            snowfall = hourlySnowfall[it],
            weatherCode = hourlyWeatherCode[it]
        )
    }

    // group hourly values into separate dates
    val dailyForecasts: List<DailyForecast> = hourlyValues.groupBy { it.dateTime.date }
        .map { (date, measurements) -> DailyForecast(date, measurements) }

    logger.debug { "Created daily forecast list" }

    return Forecast(dailyForecasts, units)
}

// exposed function to retrieve parsed weather forecast data from long and lat
fun getForecast(latitude: Double, longitude: Double): Forecast {
    val url = buildUrl(latitude, longitude)
    val body = getRawWeatherData(url)
    val forecast = convertToForecastObject(body)

    return forecast
}