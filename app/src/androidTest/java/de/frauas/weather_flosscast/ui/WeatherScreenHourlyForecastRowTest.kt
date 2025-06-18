package de.frauas.weather_flosscast.ui

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.printToLog
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.generateMockForecast
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

class WeatherScreenHourlyForecastRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    lateinit var randomMockForecast : Forecast
    var time : Int = 1

    @Before
    fun randomForecast(){
        randomMockForecast = generateMockForecast()
        time = randomMockForecast.timestamp.hour
    }

    lateinit var randomForecast : Forecast
    @Before
    fun createRandomForecast(){
        randomForecast = generateMockForecast()
    }

    @Test
    fun forecast_checkIf24ItemsAreCreated(){
        composeTestRule.setContent {
            HourlyForecastRow(randomForecast)
        }
        composeTestRule.onRoot().printToLog("ForecastRow")
        for (i in 1..3) {
            composeTestRule.onAllNodesWithText("Uhr", substring = true).assertCountEquals(6)
            composeTestRule.onNode(hasScrollAction()).performScrollToIndex(i * 6)
            composeTestRule.onRoot().printToLog("ForecastRow Now")
        }
        Thread.sleep(5000)
    }

    @Test
    fun forecast_checkIfAllItemsAreInRightSequence(){
        composeTestRule.setContent {
            HourlyForecastRow(randomForecast)
        }
        var testTime = time
        composeTestRule.onRoot().printToLog("ForecastRow")
        for (i in 1..3) {
            for (k in 1..6){
                composeTestRule.onNodeWithText("${testTime} Uhr").assertExists()
                testTime++
                if (testTime==24) testTime =0
            }
            composeTestRule.onNode(hasScrollAction()).performScrollToIndex(i * 6)
            composeTestRule.onRoot().printToLog("ForecastRow Now")
        }
        Thread.sleep(5000)
    }
}