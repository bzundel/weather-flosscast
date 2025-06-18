package de.frauas.weather_flosscast.ui

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.generateMockForecast
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
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
        composeTestRule.onNodeWithText(" Uhr").assertTextEquals("$randomHour Uhr")
    }

    @Test
    fun hourlyItemCorrectTemperature(){
        composeTestRule.setContent {
            HourlyItem(randomMockForecast, randomHour)
        }
        val temperature = randomMockForecast.getHourlyData(randomHour)!!.temp
        composeTestRule.onNodeWithText("${temperature}").assertExists().assertIsDisplayed()
    }

}