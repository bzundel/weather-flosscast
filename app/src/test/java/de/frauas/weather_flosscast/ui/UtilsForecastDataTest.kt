package de.frauas.weather_flosscast.ui

import de.frauas.weather_flosscast.DailyForecast
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.Hourly
import de.frauas.weather_flosscast.Units
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.roundToInt


class UtilsForecastDataTest{
    lateinit var mockForecast : Forecast
    lateinit var mockMinMaxForecast : Forecast
    var customTemperature : Double = 0.0
    var customMinTemperature : Double = -10.0
    var customMaxTemperature : Double = 40.0

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

    fun generateSpecifiedMinMaxMockForecast(customTemperature: Double, customMinTemperature: Double, customMaxTemperature: Double): Forecast {
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

            val dateTime0 = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0)

            Hourly(
                dateTime = dateTime0,
                temperature = customMinTemperature,
                relativeHumidity = Random.nextInt(40, 90),
                precipitationProbability = Random.nextInt(0, 60),
                rain = 0.0,
                showers = 0.0,
                snowfall = 0.0,
                weatherCode = Random.nextInt(0, 100)
            )

            val dateTime1 = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 0, 0)

            Hourly(
                dateTime = dateTime1,
                temperature = customMaxTemperature,
                relativeHumidity = Random.nextInt(40, 90),
                precipitationProbability = Random.nextInt(0, 60),
                rain = 0.0,
                showers = 0.0,
                snowfall = 0.0,
                weatherCode = Random.nextInt(0, 100)
            )

            val hourlyValues = (2 until 24).map { hour ->
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

    @Before
    fun initMockForecast(){
        customTemperature = Random.nextInt(-5, 30).toDouble()
        customMinTemperature = Random.nextInt(-25, -10).toDouble()
        customTemperature = Random.nextInt(31, 42).toDouble()
        mockForecast = generateSpecifiedMockForecast(customTemperature)
        mockMinMaxForecast = generateSpecifiedMinMaxMockForecast(customTemperature, customMinTemperature, customMaxTemperature)
    }

    @Test
    fun testGetCurrentTemperature(){
        assertEquals(mockForecast.getCurrentTemperature(), customTemperature.roundToInt())
    }

    @Test
    fun testGetDailyMinTemperature(){
        assertEquals(mockMinMaxForecast.getDailyMinTemp(), customMinTemperature.roundToInt())
    }

    @Test
    fun testGetMaxTemperature(){
        assertEquals(mockMinMaxForecast.getDailyMaxTemp(), customMaxTemperature.roundToInt())
    }

    @Test
    fun testGetDailyData(){
        val testDailyData = mockMinMaxForecast.getDailyData(0)
        assertEquals(testDailyData.dayLabel, "Heute")
        assertEquals(testDailyData.max, customMaxTemperature)
        assertEquals(testDailyData.min, customMinTemperature)
    }
}