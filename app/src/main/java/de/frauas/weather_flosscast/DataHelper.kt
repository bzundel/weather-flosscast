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

/**
 * Trim double value to string
 *
 * Takes some double value and returns it as a string, stripped down to one digit after the dot.
 *
 * @param value double to trim and convert to string
 * @return a string representing the double value with one digit after the dot
 */
private fun stripCoordinate(value: Double): String {
    return String.format(Locale.ENGLISH, "%.1f", value)
}

/**
 * Create coordinate tuple string
 *
 * Takes a latitude and longitude value and substitutes them to into a string, separated by a colon. Required format for accessing the cache object.
 *
 * @param latitude latitude part of the coordinate
 * @param longitude longitude part of the coordinate
 * @return a string containing both latitude and longitude separated by a colon
 */
private fun coordinateToCacheFormat(latitude: Double, longitude: Double): String = "${stripCoordinate(latitude)}:${stripCoordinate(longitude)}"

/**
 * Build forecast request URL
 *
 * Takes a latitude and longitude value and substitutes them into the desired request URL to the open-meteo forecast API. Ensures that all required information is requested in the URL.
 *
 * @param latitude latitude part of the coordinate
 * @param longitude longitude part of the coordinate
 * @return a string containing the request URL to the open-meteo forecast API with the latitude and longitude fields filled out
 */
private fun buildForecastUrl(latitude: Double, longitude: Double): String {
    return "https://api.open-meteo.com/v1/forecast?latitude=${stripCoordinate(latitude)}&longitude=${stripCoordinate(longitude)}&daily=sunrise,sunset&hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,rain,showers,snowfall"
}

/**
 * Build geocoding request URL
 *
 * Takes a latitude and longitude value and substitutes them into the desired request URL to the open-meteo geocoding API. Ensures that all required information is requested in the URL.
 *
 * @param search city name (sub)string
 * @return a string containing the request URL to the open-meteo geocoding API with the latitude and longitude fields filled out
 */
private fun buildGeocodingUrl(search: String): String {
    return "https://geocoding-api.open-meteo.com/v1/search?name=${search}&count=10&language=en&format=json"
}

/**
 * Send a request to the given URL and return the raw response
 *
 * A helper function that takes care of a safe request process with the API. Ensures all exceptions and unexpected behaviors are logged and forwarded.
 *
 * @param requestUrl the URL to make a request to
 * @return the raw response from the endpoint if the request was successful
 * @throws IOException if the request failed
 */
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

/**
 * Get key or throw an exception if it doesn't exist
 *
 * Extension function to the [JsonObject] class for retrieving a value by key, throwing an [IllegalArgumentException] if the key does not exist.
 *
 * @param key key of the desired value
 * @return a [JsonElement] if the key was found
 * @throws IllegalArgumentException if the key is not found
 */
private fun JsonObject.getOrThrow(key: String): JsonElement =
    this[key] ?: run {
        val message = "Key '${key} not found in JsonObject"
        logger.error { message }

        throw IllegalArgumentException(message)
    }

/**
 * Converts raw API response to a [Forecast] object
 *
 * Takes in the raw response from the API and manually parses the JSON to a [Forecast] object.
 *
 * @param body raw JSON response from API
 * @return a [Forecast] object on successful parsing
 * @throws SerializationException if the passed JSON cannot be parsed
 * @throws IllegalArgumentException if a requested key does not exist in a [JsonObject]
 * @throws IllegalStateException if the response arrays have a mismatch in length
 */
private fun convertToForecastObject(body: String): Forecast {
    val json: JsonObject = try {
        Json.parseToJsonElement(body).jsonObject
    } catch (e: SerializationException) {
        logger.error { "Could not parse received JSON. ${e.message}" }

        throw SerializationException("Could not parse to objects", e)
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

        throw IllegalStateException("Retrieved lists from API are of different sizes. Weird things are happening.", e)
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

    val dates = hourlyValues.map { it.dateTime.date }.toSet().sorted()

    // group hourly values into separate dates
    val dailyForecasts: List<DailyForecast> = hourlyValues.groupBy { it.dateTime.date }
        .map { (date, measurements) -> DailyForecast(date
            , measurements
            , sunrise[dates.indexOf(dates.first { it == date })].toInstant(TimeZone.UTC).toLocalDateTime(currentTimezone)
            , sunset[dates.indexOf(dates.first { it == date })].toInstant(TimeZone.UTC).toLocalDateTime(currentTimezone)) }

    logger.info { "Created daily forecast list" }

    val currentTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    return Forecast(currentTime, dailyForecasts, units)
}

/**
 * Retrieve [Forecast] object from the API
 *
 * A helper function that bundles the API request process. Makes a request to the forecast endpoint, parses the response and returns the [Forecast] object.
 *
 * @param latitude latitude part of the coordinate
 * @param longitude longitude part of the coordinate
 * @return retrieved [Forecast] object
 */
private fun downloadForecast(latitude: Double, longitude: Double): Forecast {
    val url = buildForecastUrl(latitude, longitude)
    val body = getRawResponse(url)
    val forecast = convertToForecastObject(body)

    return forecast
}

/**
 * Retrieve [Forecast] object from cache of from the API
 *
 * Exposed helper function to bundle the entire retrieval process. Initially checks the cache for a usable target and falls back to making a request to the API.
 *
 * @param appDir [File] object pointing to the the files directory of the app
 * @param latitude latitude part of the coordinate
 * @param longitude longitude part of the coordinate
 * @param forceUpdate ignore cache age checks and always pull from internet
 * @param updateOnlyIfTrue i literally have no idea what this does, ask patryk
 * @return [Forecast] object retrieved from the cache or API
 * @throws SerializationException if the JSON from the API or cache cannot be parsed
 * @throws IllegalArgumentException if a requested key does not exist in a [JsonObject] during parsing
 * @throws IllegalStateException insane behavior that should never happen
 */
suspend fun getForecastFromCacheOrDownload(appDir: File, latitude: Double, longitude: Double, forceUpdate: Boolean = false, updateOnlyIfTrue : Boolean = true): Forecast {
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

            throw SerializationException("Could not parse cache to array", e)
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

            // check if cache value is older than an hour or an update is desired in the first place
            if ((shouldUpdateCache(cacheForecast.timestamp) || forceUpdate) && updateOnlyIfTrue) {
                // update cached value
                val currentForecast: Forecast = downloadForecast(latitude, longitude)
                val currentForecastJson = serializeForecast(currentForecast)
                val updatedCacheForecastList: JsonObject = JsonObject(cacheForecastList + (coordinatesCacheFormat to currentForecastJson))
                cacheFile.writeText(updatedCacheForecastList.toString())

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

/**
 * Check age of cache
 *
 * Compares the cache entry timestamp to the current one and returns true if the cache entry is older than an hour.
 *
 * @param cacheTimestamp timestamp from cache entry
 * @return true if [cacheTimestamp] is older than an hour
 */
private fun shouldUpdateCache(cacheTimestamp: LocalDateTime): Boolean {
    val currentTimezone: TimeZone = TimeZone.currentSystemDefault()
    val currentTimestamp = Clock.System.now().toLocalDateTime(currentTimezone)

    val cacheInstant = cacheTimestamp.toInstant(currentTimezone)
    val currentInstant = currentTimestamp.toInstant(currentTimezone)

    val duration: Duration = (currentInstant - cacheInstant).absoluteValue

    return duration > 1.hours
}

/**
 * Serialize [Forecast] to [JsonObject]
 *
 * @param forecast [Forecast] object to serialize
 * @return [JsonObject] representation of forecast
 */
fun serializeForecast(forecast: Forecast): JsonObject {
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

/**
 * Deserialize [JsonObject] to [Forecast]
 *
 * @param json [JsonObject] to deserialize
 * @return [Forecast] object representation of passed [JsonObject]
 * @throws IllegalArgumentException if a key cannot be found in passed [JsonObject]
 */
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

/**
 * Retrieve [City] list from name (sub)string
 *
 * Makes a call to request 10 cities based on a (partially) complete city name. Response is parsed into a list of [City].
 *
 * @param search (sub)string of the desired city name
 * @return list of [City] with max. size 10 matching the substring
 * @throws SerializationException if response JSON cannot be parsed
 * @throws IllegalArgumentException if key does not exist in [JsonObject]
 */
suspend fun getCitySearchResults(search: String): List<City> {
    return withContext(Dispatchers.IO) {
        val url: String = buildGeocodingUrl(search)
        val body: String = getRawResponse(url)

        val json: JsonObject = try {
            Json.parseToJsonElement(body).jsonObject
        } catch (e: SerializationException) {
            logger.error { "Could not parse received JSON. ${e.message}" }

            throw SerializationException("Could not parse to objects", e)
        }

        logger.info { "Deserialized API response" }

        val cities = json.getOrThrow("results").jsonArray.map {
            val cityJson: JsonObject = it.jsonObject

            City(
                cityName = cityJson.getOrThrow("name").jsonPrimitive.content,
                state = cityJson.getOrThrow("admin1").jsonPrimitive.content,
                country = cityJson.getOrThrow("country").jsonPrimitive.content,
                latitude = cityJson.getOrThrow("latitude").jsonPrimitive.content.toDouble(),
                longitude = cityJson.getOrThrow("longitude").jsonPrimitive.content.toDouble(),
            )
        }

        logger.info { "Parsed JSON to objects" }

        return@withContext cities
    }
}
