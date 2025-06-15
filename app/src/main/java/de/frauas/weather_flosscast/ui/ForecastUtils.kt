package de.frauas.weather_flosscast.ui

import android.content.Context
import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.getForecastFromCacheOrDownload
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours

/**
     * Function to get the current temperature
     */
    fun Forecast.getCurrentTemperature(): Int? {//Utility function to update newest temperature
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val currentHour = now.hour

        val todayForecast = days.firstOrNull { it.date == today } ?: return 0

        return todayForecast.hourlyValues.firstOrNull { it.dateTime.hour == currentHour }?.temperature?.roundToInt()
    }
    /**
     * Function to get the daily min temperature
     */
    fun Forecast.getDailyMinTemp(): Int {

        val allTemps = mutableListOf<Double>()

        for (daily in days) {
            // add all temps to the list
            allTemps.addAll(daily.hourlyValues.map { it.temperature })
        }
        return allTemps.minOrNull()?.roundToInt() ?: 0
    }
    /**
     * Function to get the daily max temperature
     */
    fun Forecast.getDailyMaxTemp(): Int {

        val allTemps = mutableListOf<Double>()

        for (daily in days) {
            // add all temps to the list
            allTemps.addAll(daily.hourlyValues.map { it.temperature })
        }
        return allTemps.maxOrNull()?.roundToInt() ?: 0
    }

data class HourlyData(val hour: Int, val state : Int, val temp: Int)//Dataconstruct for HourlyData
fun Forecast.getHourlyData(hour: Int): HourlyData? {
    val timeZone = TimeZone.currentSystemDefault()

    // JNow-Timezone data
    val nowInstant = Clock.System.now()

    // +hour adding for different data
    val targetInstant = nowInstant.plus(hour.hours)

    // Converting to local data
    val targetDateTime = targetInstant.toLocalDateTime(timeZone)

    // Setting today as val
    val targetDay = days.firstOrNull { it.date == targetDateTime.date } ?: return null

    // Setting hour as val
    val hourly = targetDay.hourlyValues.firstOrNull { it.dateTime.hour == targetDateTime.hour } ?: return null

    return HourlyData(
        hour = targetDateTime.hour, //Taking the right values from set values
        state = hourly.weatherCode,
        temp = hourly.temperature.roundToInt()
    )
}
//Getting data for DailyItem List
data class DailyData(val dayLabel : String, val state : Int, val rain : Int, val max: Int, val min: Int)
fun Forecast.getDailyData(day: Int): DailyData {
    if (day >= days.size) return DailyData("Unbekannt", 0, 0, 0, 0)

    val targetDay = days[day]
    val date = targetDay.date

    val weekdayLabel = if (day == 0) "Heute" else when (date.dayOfWeek) {
        DayOfWeek.MONDAY    -> "Montag"
        DayOfWeek.TUESDAY   -> "Dienstag"
        DayOfWeek.WEDNESDAY -> "Mittwoch"
        DayOfWeek.THURSDAY  -> "Donnerstag"
        DayOfWeek.FRIDAY    -> "Freitag"
        DayOfWeek.SATURDAY  -> "Samstag"
        DayOfWeek.SUNDAY    -> "Sonntag"
    }

    val weatherCode = targetDay.hourlyValues.firstOrNull()?.weatherCode ?: 0
    val rainAmount = targetDay.hourlyValues.maxOfOrNull { it.precipitationProbability } ?: 0
    val maxTemp = targetDay.hourlyValues.maxOfOrNull { it.temperature }?.roundToInt() ?: 0
    val minTemp = targetDay.hourlyValues.minOfOrNull { it.temperature }?.roundToInt() ?: 0

    return DailyData(
        dayLabel = weekdayLabel,
        state = weatherCode,
        rain = rainAmount,
        max = maxTemp,
        min = minTemp
    )
}

/**
 * Loads for all `cities` the forecast out of cache or download and gives a map back with the key cityName.
 */
suspend fun loadForecastsForCities(context: Context,cities: List<City>): Map<String, Forecast> {
    val appDir = context.filesDir
    val result = mutableMapOf<String, Forecast>()
    for (city in cities) {
        try {
            val fc = getForecastFromCacheOrDownload(appDir, city.latitude, city.longitude, true)
            result[city.cityName] = fc
        } catch (_: Exception) {
            // Fehler pro Stadt ignorieren
        }
    }
    return result
}