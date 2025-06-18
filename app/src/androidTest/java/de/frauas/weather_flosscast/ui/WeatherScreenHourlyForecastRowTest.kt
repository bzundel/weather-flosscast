package de.frauas.weather_flosscast.ui

import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.generateMockForecast
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherScreenHourlyForecastRowTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var randomForecast : Forecast
    @Before
    fun createRandomForecast(){
        randomForecast = generateMockForecast()
    }

//    @Test
//    fun checkIf24ItemsAreCreated(){
//        composeTestRule.setContent {
//            HourlyForecastRow(randomForecast)
//        }
//        composeTestRule.onNode(hasScrollAction()).
//    }
}