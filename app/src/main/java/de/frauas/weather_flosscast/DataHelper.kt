package de.frauas.weather_flosscast

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private fun stripCoordinate(value: Double): String {
    return String.format(Locale.ENGLISH, "%.2f", value)
}

private fun buildUrl(latitude: Double, longitude: Double): String {
    return "https://api.open-meteo.com/v1/forecast?latitude=${stripCoordinate(latitude)}&longitude=${stripCoordinate(longitude)}&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,rain,showers,snowfall"
}

private fun getRawWeatherData(requestUrl: String): String {
    val url = URL(requestUrl)
    val connection = url.openConnection() as HttpURLConnection

    try {
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            return connection.inputStream.bufferedReader().use { it.readText() }
        }
        else {
            // FIXME log and throw proper exception
            throw Exception("Oopsie")
        }
    } finally {
        connection.disconnect()
    }
}

private fun convertToForecastObject(body: String): Forecast {
    // FIXME wrap in try catch for NullPointerException (non-null assertion (!!.))
    val json: JsonObject = Json.parseToJsonElement(body).jsonObject

    // extract units of hourly data
    val unitsJson: JsonObject = json["hourly_units"]!!.jsonObject
    val units = Units(
        unitsJson["temperature_2m"]!!.jsonPrimitive.content,
        unitsJson["relative_humidity_2m"]!!.jsonPrimitive.content,
        unitsJson["precipitation_probability"]!!.jsonPrimitive.content,
        unitsJson["rain"]!!.jsonPrimitive.content,
        unitsJson["showers"]!!.jsonPrimitive.content,
        unitsJson["snowfall"]!!.jsonPrimitive.content
    )

    // extract hourly values and cast to respective type
    val hourly: JsonObject = json["hourly"]!!.jsonObject
    val hourlyTime = hourly["time"]!!.jsonArray.map { LocalDateTime.parse(it.jsonPrimitive.content) }
    val hourlyTemperature = hourly["temperature_2m"]!!.jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlyRelativeHumidity = hourly["relative_humidity_2m"]!!.jsonArray.map { it.jsonPrimitive.content.toInt() }
    val hourlyPrecipitationProbability = hourly["precipitation_probability"]!!.jsonArray.map { it.jsonPrimitive.content.toInt() }
    val hourlyRain = hourly["rain"]!!.jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlyShowers = hourly["showers"]!!.jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlySnowfall = hourly["snowfall"]!!.jsonArray.map { it.jsonPrimitive.content.toDouble() }
    val hourlyWeatherCode = hourly["weather_code"]!!.jsonArray.map { it.jsonPrimitive.content.toInt() }

    // assert that all extracted lists have the same length
    assert(listOf
        (hourlyTime
        , hourlyTemperature
        , hourlyRelativeHumidity
        , hourlyPrecipitationProbability
        , hourlyRain
        , hourlyShowers
        , hourlySnowfall
        , hourlyWeatherCode
        ).map { it.size }.toSet().size == 1)

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

    val dailyForecasts: List<DailyForecast> = hourlyValues.groupBy { it.dateTime.date }.map { (date, measurements) -> DailyForecast(date, measurements) }

    return Forecast(dailyForecasts, units)
}

fun getForecast(latitude: Double, longitude: Double): Forecast {
    val url = buildUrl(latitude, longitude)
    val body = getRawWeatherData(url)
    val forecast = convertToForecastObject(body)

    return forecast
}