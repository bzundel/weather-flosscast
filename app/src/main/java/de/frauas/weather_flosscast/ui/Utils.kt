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
 * Function to update newest temperature with the current time
 * @return current temperature
 */
fun Forecast.getCurrentTemperature(): Int? {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.date
    val currentHour = now.hour
    val todayForecast = days.firstOrNull { it.date == today } ?: return 0
    return todayForecast.hourlyValues.firstOrNull { it.dateTime.hour == currentHour }?.temperature?.roundToInt()
}


/**
 * Function to get the daily min temperature out of all 24 values of a day
 * @return min temperature of a day
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
 * Function to get the daily max temperature out of all 24 values of a day
 * @return max temperature of a day
 */
fun Forecast.getDailyMaxTemp(): Int {
    val allTemps = mutableListOf<Double>()
    for (daily in days) {
        // add all temps to the list
        allTemps.addAll(daily.hourlyValues.map { it.temperature })
    }
    return allTemps.maxOrNull()?.roundToInt() ?: 0
}

//Data-construct for hourly data
data class HourlyData(val hour: Int, val state : Int, val isNight : Boolean, val temp: Int)

/**
 *
 */
fun Forecast.getHourlyData(offsetHours: Int): HourlyData? {
    val tz = TimeZone.currentSystemDefault()
    //Adding hours to the targetTimeZone. In Util function the forecast data will be different directly
    val targetDt = Clock.System.now().plus(offsetHours.hours).toLocalDateTime(tz)   //adds targetted time to our localTime
    val today = days.firstOrNull { it.date == targetDt.date } ?: return null    //gets todays data with a comparison of all days
    val hourly = today.hourlyValues.firstOrNull { it.dateTime.hour == targetDt.hour } ?: return null    //gets hourly data comparing hours on the list
    val sunrise: kotlinx.datetime.LocalDateTime = today.sunrise //gets sunrise, sunset data
    val sunset:  kotlinx.datetime.LocalDateTime = today.sunset
    val isNight = targetDt < sunrise || targetDt > sunset   //isnight of the offset-Time if time is in between sunrise, sunset

    return HourlyData(
        hour    = targetDt.hour,
        state   = hourly.weatherCode,
        isNight = isNight,
        temp    = hourly.temperature.roundToInt()
    )
}


//Data-construct for daily data
data class DailyData(val dayLabel : String, val state : Int, val rain : Int, val max: Int, val min: Int)


/**
 *
 */
fun Forecast.getDailyData(day: Int): DailyData {
    if (day >= days.size) return DailyData("Unbekannt", 0, 0, 0, 0) //if day is higher than days.size in Forecast(7) return value is 0

    val targetDay = days[day]       //gets the right time forecast data
    val date = targetDay.date       //gets target time from forecast
    val weekdayLabel = if (day == 0) "Heute" else when (date.dayOfWeek) {   //sets days labels accordingly to day data in forecast
        DayOfWeek.MONDAY    -> "Mo."
        DayOfWeek.TUESDAY   -> "Di."
        DayOfWeek.WEDNESDAY -> "Mi."
        DayOfWeek.THURSDAY  -> "Do."
        DayOfWeek.FRIDAY    -> "Fr."
        DayOfWeek.SATURDAY  -> "Sa."
        DayOfWeek.SUNDAY    -> "So."
    }
    val weatherCode = targetDay.hourlyValues.maxByOrNull { it.weatherCode }?.weatherCode ?: 0   //gets biggest weatherCode number of the day(Worst-Case scenario)
    val rainProbability = targetDay.hourlyValues.maxOfOrNull { it.precipitationProbability } ?: 0   //gets max value of rainProbability of the day
    val maxTemp = targetDay.hourlyValues.maxOfOrNull { it.temperature }?.roundToInt() ?: 0  //gets the max temperature of the day
    val minTemp = targetDay.hourlyValues.minOfOrNull { it.temperature }?.roundToInt() ?: 0  //gets the min temperature of the day

    return DailyData(   //return data
        dayLabel = weekdayLabel,
        state = weatherCode,
        rain = rainProbability,
        max = maxTemp,
        min = minTemp
    )
}

/**
 * Function returns the weathercode and if it is night or not at the current time
 * @return wmoCode & isNight : Boolean
 */
fun Forecast.getWmoCodeAndIsNight(): Pair<Int, Boolean> {
    val today = days.firstOrNull() ?: return 0 to false

    //  Check if night or day
    val sunrise = LocalDateTime.parse(today.sunrise.toString())     //Getting sunrise and sunset time from the api
    val sunset  = LocalDateTime.parse(today.sunset.toString())
    val now     = LocalDateTime.now()
    val isNight = now.isBefore(sunrise) || now.isAfter(sunset)      //Set night to true if time is before sunrise or after sunset

    val wmoCode = today.hourlyValues.firstOrNull()?.weatherCode ?: 0    //get current WeatherCode

    return wmoCode to isNight
}

/**
 * Loads for all cities the forecast out of cache or download and gives a map back with the key cityName.
 * @return list of cities and their forecasts
 */
suspend fun loadForecastsForCities(context: Context,cities: List<City>,forceRefresh : Boolean): Map<String, Forecast> {
    val appDir = context.filesDir
    val result = mutableMapOf<String, Forecast>()
    for (city in cities) {      //updated forecast through the city list
        try {
            val fc = getForecastFromCacheOrDownload(appDir, city.latitude, city.longitude, forceRefresh)
            result[city.cityName] = fc
        } catch (_: Exception) {
        // ignore error
         }
    }
    return result   //return updated cities
}

/**
 * Function to get the right lottie-animation with the @params wmo-code and isNight?
 * @return lottie-animation(.json)
 */
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
            in 80..89                    -> R.raw.rain              // Drizzle / Rain / Showers
            else                               -> R.raw.wolken            // Nebel, Staub, sonstige Wolken
        }
    }
}

/**
 * Function for get the right icon based on weather-code and if its night or not
 * @return icon (.xml)
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
 *  Function to get the information text out of the weather-code
 *  @return condition : String
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

/**
 * Function to get the right color for the given weather-code
 * @return Color
 */
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