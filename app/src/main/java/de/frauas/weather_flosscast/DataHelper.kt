package de.frauas.weather_flosscast

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

fun getRawWeatherData(): String {
    val url = URL("https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m")
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

fun convertToForecastObject(body: String): Forecast {
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
