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


//Tests the defined helper functions for forecast data handling from Utils.kt
class UtilsForecastDataTest{
    //Mock variables
    lateinit var mockForecast : Forecast
    lateinit var mockMinMaxForecast : Forecast
    var customTemperature : Double = 0.0
    var customMinTemperature : Double = -10.0
    var customMaxTemperature : Double = 40.0

    //Because of an unit test the function got copied from MockForecast.kt
    //See MockForecast.kt for comments to this function
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

    //Because of an unit test the function got copied from MockForecast.kt
    //See MockForecast.kt for comments to this function
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

    //Sets random values for all the mock variables before each test
    @Before
    fun initMockForecast(){
        customTemperature = Random.nextInt(-5, 30).toDouble()
        customMinTemperature = Random.nextInt(-25, -10).toDouble()
        customTemperature = Random.nextInt(31, 42).toDouble()
        mockForecast = generateSpecifiedMockForecast(customTemperature)
        mockMinMaxForecast = generateSpecifiedMinMaxMockForecast(customTemperature, customMinTemperature, customMaxTemperature)
    }

    //Tests if the correct current temperature gets returned
    @Test
    fun testGetCurrentTemperature(){
        //The mock forecast got created with the same temprature for all values, therefore the mock temperature gets compared to the return of the getCurrentTemp function
        assertEquals(mockForecast.getCurrentTemperature(), customTemperature.roundToInt())
    }

    //Tests if the correct minimum temperature gets returned
    @Test
    fun testGetDailyMinTemperature(){
        //In the mock forecast every day has the same minimun Temperature. The returned value gets compared to the set mock minimum temperature
        assertEquals(mockMinMaxForecast.getDailyMinTemp(), customMinTemperature.roundToInt())
    }

    //Tests if the correct maximum temperature gets returned
    @Test
    fun testGetMaxTemperature(){
        //In the mock forecast every day has the same maximum Temperature. The returned value gets compared to the set mock maximum temperature
        assertEquals(mockMinMaxForecast.getDailyMaxTemp(), customMaxTemperature.roundToInt())
    }

    //Tests if the DailyData has the right values
    @Test
    fun testGetDailyData(){
        //Runs function
        val testDailyData = mockMinMaxForecast.getDailyData(0)
        //Test if the fields dayLabel, max, min are the correct values
        assertEquals(testDailyData.dayLabel, "Heute")
        assertEquals(testDailyData.max, customMaxTemperature)
        assertEquals(testDailyData.min, customMinTemperature)
    }
}