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
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

private val logger = KotlinLogging.logger { }

// convert double to string with two digits after the decimal point
private fun stripCoordinate(value: Double): String {
    return String.format(Locale.ENGLISH, "%.1f", value)
}

private fun coordinateToCacheFormat(latitude: Double, longitude: Double) = "${stripCoordinate(latitude)}:${stripCoordinate(longitude)}"

// build forecast request url from latitude and longitude values
private fun buildForecastUrl(latitude: Double, longitude: Double): String {
    return "https://api.open-meteo.com/v1/forecast?latitude=${stripCoordinate(latitude)}&longitude=${stripCoordinate(longitude)}&daily=sunrise,sunset&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,rain,showers,snowfall"
}

// build geocoding request url from latitude and longitude values
private fun buildGeocodingUrl(search: String): String {
    return "https://geocoding-api.open-meteo.com/v1/search?name=${search}&count=10&language=en&format=json"
}

// make a call to the request url and return the raw response as a string
private fun getRawResponse(requestUrl: String): String {
    val url = URL(requestUrl)
    var connection: HttpURLConnection? = null

    try {
        connection = url.openConnection() as HttpURLConnection

        logger.info { "Connection opened successfully" }

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

        logger.info { "Connection closed" }
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

    logger.info{ "Parsed units" }

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

    logger.info { "Parsed hourly data" }

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

    logger.info { "Passed list size assertion" }

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

    val daily: JsonObject = json["daily"]!!.jsonObject
    val sunrise: List<LocalDateTime> = daily.getOrThrow("sunrise").jsonArray.map { LocalDateTime.parse(it.jsonPrimitive.content) }
    val sunset: List<LocalDateTime> = daily.getOrThrow("sunset").jsonArray.map { LocalDateTime.parse(it.jsonPrimitive.content) }

    val currentTimezone: TimeZone = TimeZone.currentSystemDefault()

    // group hourly values into separate dates
    val dailyForecasts: List<DailyForecast> = hourlyValues.groupBy { it.dateTime.date }
        .map { (date, measurements) -> DailyForecast(date
            , measurements
            , sunrise.first { it.date == date }.toInstant(TimeZone.UTC).toLocalDateTime(currentTimezone)
            , sunset.first { it.date == date }.toInstant(TimeZone.UTC).toLocalDateTime(currentTimezone)) }

    logger.info { "Created daily forecast list" }

    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return Forecast(currentTime, dailyForecasts, units)
}

private fun downloadForecast(latitude: Double, longitude: Double): Forecast {
    val url = buildForecastUrl(latitude, longitude)
    val body = getRawResponse(url)
    val forecast = convertToForecastObject(body)

    return forecast
}

// exposed function to get forecast from cache or api
suspend fun getForecastFromCacheOrDownload(appDir: File, latitude: Double, longitude: Double): Forecast {
    return withContext(Dispatchers.IO) {
        val cacheFile: File = File(appDir, "cache.json")

        // create cache file containing empty jsonobject if none exists
        if (!cacheFile.exists()) {
            val emptyJsonObjectString: String = Json.encodeToString(JsonObject(emptyMap()))
            cacheFile.writeText(emptyJsonObjectString)

            logger.info { "Created empty cache file" }
        }

        val jsonString: String = cacheFile.readText()

        // parse cache file to jsonobject
        val cacheForecastList: JsonObject = try {
            Json.parseToJsonElement(jsonString).jsonObject
        } catch (e: SerializationException) {
            logger.error { "Could not parse cache JSON. ${e.message}" }

            throw Exception("Could not parse cache to array", e)
        }

        logger.info { "Parsed cache file successfully" }

        val coordinatesCacheFormat = coordinateToCacheFormat(latitude, longitude)

        // filter cache list for matches
        val cachedMatches: List<JsonObject> = cacheForecastList.filter { it.key == coordinatesCacheFormat }.map { it.value.jsonObject }

        if (cachedMatches.isEmpty()) {
            // if no matches, download from api and save current forecast to cache
            logger.info { "No matching cached entries found" }

            val forecast: Forecast = downloadForecast(latitude, longitude)
            val forecastJson: JsonObject = serializeForecast(forecast)
            val updatedCacheForecastList: JsonObject = JsonObject(cacheForecastList + (coordinatesCacheFormat to forecastJson))
            cacheFile.writeText(updatedCacheForecastList.toString())

            logger.info { "Wrote current forecast for coordinates $coordinatesCacheFormat to cache" }

            return@withContext forecast
        } else {
            if (cachedMatches.size > 1) { // should technically not be possible. sanity check
                logger.error { "Found multiple entries for coordinate in cache file." }

                throw IllegalStateException("Multiple entries for same coordinate were found in cache file.")
            }

            // direct match found
            val match: JsonObject = cachedMatches.first()
            val cacheForecast: Forecast = deserializeForecast(match)

            logger.info { "Successfully deserialized cached forecast" }

            // check if cache value is older than an hour
            if (shouldUpdateCache(cacheForecast.timestamp)) {
                // update cached value
                val currentForecast: Forecast = downloadForecast(latitude, longitude)
                val currentForecastJson = serializeForecast(currentForecast)
                val updatedCacheForecastList: JsonObject = JsonObject(cacheForecastList + (coordinatesCacheFormat to currentForecastJson))
                cacheFile.writeText(updatedCacheForecastList.toString())
                // FIXME maybe outsource to function? same code twice

                logger.info { "Updated forecast cache for coordinates $coordinatesCacheFormat" }

                return@withContext currentForecast
            } else {
                // return cached value
                logger.info { "Returning cached forecast" }

                return@withContext cacheForecast
            }
        }
    }
}

// check if cached value is more than an hour old
private fun shouldUpdateCache(cacheTimestamp: LocalDateTime): Boolean {
    val currentTimezone: TimeZone = TimeZone.currentSystemDefault()
    val currentTimestamp = Clock.System.now().toLocalDateTime(currentTimezone)

    val cacheInstant = cacheTimestamp.toInstant(currentTimezone)
    val currentInstant = currentTimestamp.toInstant(currentTimezone)

    val duration: Duration = (currentInstant - cacheInstant).absoluteValue

    return duration > 1.hours
}

// serialize forecast object to jsonobject for writing
private fun serializeForecast(forecast: Forecast): JsonObject {
    return buildJsonObject {
        put("timestamp", LocalDateTime.Formats.ISO.format(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())))
        put("units", buildJsonObject {
            put("temperature", forecast.units.temperature)
            put("humidity", forecast.units.humidity)
            put("precipitationProbability", forecast.units.precipitationProbability)
            put("rain", forecast.units.rain)
            put("showers", forecast.units.showers)
            put("snow", forecast.units.snow)
        })
        put("days", buildJsonArray {
            forecast.days.forEach {
                add(buildJsonObject {
                    put("date", LocalDate.Formats.ISO.format(it.date))
                    put("hourlyValues", buildJsonArray {
                        it.hourlyValues.forEach {
                            add(buildJsonObject {
                                put("dateTime", LocalDateTime.Formats.ISO.format(it.dateTime))
                                put("temperature", it.temperature)
                                put("relativeHumidity", it.relativeHumidity)
                                put("precipitationProbability", it.precipitationProbability)
                                put("rain", it.rain)
                                put("showers", it.showers)
                                put("snowfall", it.snowfall)
                                put("weatherCode", it.weatherCode)

                            })
                        }
                    })
                    put("sunrise", it.sunrise.toString())
                    put("sunset", it.sunset.toString())
                })
            }
        })
    }
}

// deserialize jsonobject to forecast object for reading
private fun deserializeForecast(json: JsonObject): Forecast {
    val timestamp: LocalDateTime =
        LocalDateTime.parse(json.getOrThrow("timestamp").jsonPrimitive.content)

    val unitsJson: JsonObject = json.getOrThrow("units").jsonObject
    val units = Units(
        unitsJson.getOrThrow("temperature").jsonPrimitive.content,
        unitsJson.getOrThrow("humidity").jsonPrimitive.content,
        unitsJson.getOrThrow("precipitationProbability").jsonPrimitive.content,
        unitsJson.getOrThrow("rain").jsonPrimitive.content,
        unitsJson.getOrThrow("showers").jsonPrimitive.content,
        unitsJson.getOrThrow("snow").jsonPrimitive.content,
    )

    val days: List<DailyForecast> = json.getOrThrow("days").jsonArray.map { dailyElement ->
        val daily: JsonObject = dailyElement.jsonObject

        DailyForecast(
            LocalDate.parse(daily.getOrThrow("date").jsonPrimitive.content),
            daily.getOrThrow("hourlyValues").jsonArray.map {
                val hourly: JsonObject = it.jsonObject

                Hourly(
                    LocalDateTime.parse(hourly.getOrThrow("dateTime").jsonPrimitive.content),
                    hourly.getOrThrow("temperature").jsonPrimitive.content.toDouble(),
                    hourly.getOrThrow("relativeHumidity").jsonPrimitive.content.toInt(),
                    hourly.getOrThrow("precipitationProbability").jsonPrimitive.content.toInt(),
                    hourly.getOrThrow("rain").jsonPrimitive.content.toDouble(),
                    hourly.getOrThrow("showers").jsonPrimitive.content.toDouble(),
                    hourly.getOrThrow("snowfall").jsonPrimitive.content.toDouble(),
                    hourly.getOrThrow("weatherCode").jsonPrimitive.content.toInt(),
                )
            },
            LocalDateTime.parse(daily.getOrThrow("sunrise").jsonPrimitive.content),
            LocalDateTime.parse(daily.getOrThrow("sunset").jsonPrimitive.content)
        )
    }

    return Forecast(timestamp, days, units)
}

// make a request to the open-meteo geocoding api and return the matches
suspend fun getCitySearchResults(search: String): List<City> {
    return withContext(Dispatchers.IO) {
        val url: String = buildGeocodingUrl(search)
        val body: String = getRawResponse(url)

        val json: JsonObject = try {
            Json.parseToJsonElement(body).jsonObject
        } catch (e: SerializationException) {
            logger.error { "Could not parse received JSON. ${e.message}" }

            throw Exception("Could not parse to objects", e)
        }

        logger.info { "Deserialized API response" }

        val cities = json.getOrThrow("results").jsonArray.map {
            val cityJson: JsonObject = it.jsonObject

            City(
                cityName = cityJson.getOrThrow("name").jsonPrimitive.content,
                //state = cityJson.getOrThrow("admin1").jsonPrimitive.content,
                //country = cityJson.getOrThrow("country").jsonPrimitive.content,
                latitude = cityJson.getOrThrow("latitude").jsonPrimitive.content.toDouble(),
                longitude = cityJson.getOrThrow("longitude").jsonPrimitive.content.toDouble(),
            )
        } ?: emptyList()

        logger.info { "Parsed JSON to objects" }

        return@withContext cities
    }
}
