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
    return "https://api.open-meteo.com/v1/forecast?latitude=${stripCoordinate(latitude)}&longitude=${stripCoordinate(longitude)}&hourly=temperature_2m"
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

    val unit: String = json["hourly_units"]!!.jsonObject["temperature_2m"]!!.jsonPrimitive.content

    val hourly: JsonObject = json["hourly"]!!.jsonObject
    val hourlyTime = hourly["time"]!!.jsonArray.map { LocalDateTime.parse(it.jsonPrimitive.content) }
    val hourlyTemperature = hourly["temperature_2m"]!!.jsonArray.map { it.jsonPrimitive.content.toDouble() }

    assert(hourlyTime.size == hourlyTemperature.size)

    val temperatures: List<TemperatureItem> = hourlyTime.zip(hourlyTemperature).map { (time, temperature) -> TemperatureItem(time, temperature) }
    val forecasts: List<DailyForecast> = temperatures.groupBy { it.dateTime.date }.map { (date, values) -> DailyForecast(date, values) }

    return Forecast(forecasts, unit)
}

fun getForecast(latitude: Double, longitude: Double): Forecast {
    val url = buildUrl(latitude, longitude)
    val body = getRawWeatherData(url)
    val forecast = convertToForecastObject(body)

    return forecast
}