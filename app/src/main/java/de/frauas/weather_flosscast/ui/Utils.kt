package de.frauas.weather_flosscast.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.R
import de.frauas.weather_flosscast.getForecastFromCacheOrDownload
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
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






//Functions for icon in WeatherScreen
@RawRes
fun getLottieResForWmoCode(code: Int, isNight: Boolean): Int {
    return if (isNight) {
        when (code) {
            0                                  -> R.raw.mond              // klarer Himmel
            in 1..3                      -> R.raw.mondundwolken     // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.raw.gewitter          // Gewitter/Trichterwolke
            in 23..24, 26                -> R.raw.mondschnee        // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.raw.mondschnee        // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.raw.mondregen         // Drizzle / Rain / Showers
            else                               -> R.raw.mondundwolken     // Nebel, Staub, sonstige Wolken
        }
    } else {
        when (code) {
            0                                  -> R.raw.sonne             // klarer Himmel
            in 1..3                      -> R.raw.sonnewolken       // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.raw.gewitter          // Gewitter/Trichterwolke
            in 23..24, 26                -> R.raw.schnee            // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.raw.schnee            // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.raw.sonne             // Drizzle / Rain / Showers
            else                               -> R.raw.wolken            // Nebel, Staub, sonstige Wolken
        }
    }
}

// -----------------------------------------------------------------------------
// Functions for select the background-colour
// -----------------------------------------------------------------------------


/**
 * Function for get the right icon based on weather-code and if its night or not
 */
@DrawableRes
fun getIconForWmoCode(code: Int, isNight: Boolean): Int {
    return if (isNight) {
        when (code) {
            0                                  -> R.drawable.monn          // klarer Himmel
            in 1..3                      -> R.drawable.cloud_moon    // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.drawable.storm         // Gewitter/Trichterwolke
            in 23..24, 26                -> R.drawable.snowrain      // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.drawable.snow          // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.drawable.rain          // Drizzle / Rain / Showers
            else                               -> R.drawable.cloud_moon    // Nebel, Staub, sonstige Wolken
        }
    } else {
        when (code) {
            0                                  -> R.drawable.sun           // klarer Himmel
            in 1..3                      -> R.drawable.cloud_sun     // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.drawable.storm         // Gewitter/Trichterwolke
            in 23..24, 26                -> R.drawable.snowrain      // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.drawable.snow          // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.drawable.rain          // Drizzle / Rain / Showers
            else                               -> R.drawable.cloud         // Nebel, Staub, sonstige Wolken
        }
    }
}

/**
 * Gibt eine kurze Wetterbeschreibung zum WMO-Code zurück.
 */
fun getConditionForWmoCode(code: Int): String {
    return when (code) {
        0  -> "Klar"
        1  -> "Hauptsächlich klar"
        2  -> "Teilweise bewölkt"
        3  -> "Bedeckt"
        in 45..48    -> "Nebel"
        in 51..55    -> "Nieselregen"
        in 56..57    -> "Gefrierender Nieselregen"
        in 61..65    -> "Regen"
        in 66..67    -> "Gefrierender Regen"
        in 71..75    -> "Schneefall"
        77           -> "Schneekörner"
        in 80..82    -> "Regenschauer"
        in 85..86    -> "Schneeschauer"
        in 95..99    -> "Gewitter"
        else         -> "Unbekannt"
    }
}

fun colorForWmoCode(weatherCode: Int, isNight: Boolean): Color {
    return if (isNight) {
        // Color for nighttime:
        Color(0xFF37474F)// Very dark grayish blue
    } else {
        // Colors for daytime:
        // 0xFF33AAFF -> Vivid blue for clear sky
        // 0xFF808080 -> Dark gray for rain or storm
        // 0xFFB0BEC5 -> Grayish blue for snow or clouds
        when (weatherCode) {
            0                                                                         -> Color(0xFF33AAFF) // Vivid blue
            in 1..9, in 10..19, in 30..49,in 70..79            -> Color(0xFFB0BEC5) // Grayish blue
            in 20..29, in 50..59, in 60..69, in 80..99         -> Color(0xFF808080) // Dark gray
            else                                                                      -> Color(0xFFB0BEC5) // Fallback Grayish blue
        }
    }
}

/**
 * Liest aus dem Forecast den WMO-Code der ersten Stunde und
 * bestimmt, ob es gerade Nacht ist.
 *
 * @param forecast das Forecast-Objekt
 * @return Pair( WMO-Code , isNight )
 */
 fun Forecast.getWmoCodeAndIsNight(): Pair<Int, Boolean> {
    // 1) Erstes Tages-Objekt
    val today = days.firstOrNull() ?: return 0 to false

    // 2) Tag/Nacht-Berechnung
    val sunrise = LocalDateTime.parse(today.sunrise.toString())
    val sunset  = LocalDateTime.parse(today.sunset.toString())
    val now     = LocalDateTime.now()
    val isNight = now.isBefore(sunrise) || now.isAfter(sunset)

    // 3) WMO-Code der ersten Stunde (oder 0 fallback)
    val wmoCode = today.hourlyValues.firstOrNull()?.weatherCode ?: 0

    return wmoCode to isNight
}

