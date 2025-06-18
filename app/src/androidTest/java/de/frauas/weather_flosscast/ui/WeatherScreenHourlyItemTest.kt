package de.frauas.weather_flosscast.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.generateMockForecast
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

class WeatherScreenHourlyItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var randomMockForecast : Forecast
    var randomHour : Int = 1

    @Before
    fun randomForecast(){
        randomMockForecast = generateMockForecast()
        randomHour = Random.nextInt(1,25)
    }

    @Test
    fun hourlyItemCorrectHourTest(){
        composeTestRule.setContent {
            HourlyItem(randomMockForecast, randomHour)
        }
        val time = Clock.System.now().plus(randomHour.hours).toLocalDateTime(TimeZone.currentSystemDefault()).hour
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("Temp Unmerged")
        composeTestRule.onNodeWithText("$time Uhr").assertExists()
    }

    @Test
    fun hourlyItemCorrectTemperature(){
        composeTestRule.setContent {
            HourlyItem(randomMockForecast, randomHour)
        }
        val temperature = randomMockForecast.getHourlyData(randomHour)!!.temp

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("Temp Unmerged")
        composeTestRule.onRoot().printToLog("Temp Merged")
        composeTestRule.onNodeWithText("${temperature}°").assertExists().assertIsDisplayed()
    }

}