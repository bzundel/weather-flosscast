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

    //Initialising test enviorment to test Compose Entities
    @get:Rule
    val composeTestRule = createComposeRule()

    //Mock values needed for testing
    lateinit var randomMockForecast : Forecast
    var randomHour : Int = 1

    //Initializes the Mock values with random values before each test
    @Before
    fun randomForecast(){
        randomMockForecast = generateMockForecast()
        randomHour = Random.nextInt(1,25)
    }

    //Tests if HourlyItem displays the correct time
    @Test
    fun hourlyItemCorrectHourTest(){
        //Creates the HourlyItem Compose Nodes for testing
        composeTestRule.setContent {
            HourlyItem(randomMockForecast, randomHour)
        }
        //Calculates the correct time that should be displayed
        val time = Clock.System.now().plus(randomHour.hours).toLocalDateTime(TimeZone.currentSystemDefault()).hour
        //Finds Node which displays the HourlyItem time and asserts the correct time is displayed
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("Temp Unmerged")
        composeTestRule.onNodeWithText("$time Uhr").assertExists()
    }

    //Tests if HourlyItem displays the correct temperature
    @Test
    fun hourlyItemCorrectTemperature(){
        //Creates the HourlyItem Compose Nodes for testing
        composeTestRule.setContent {
            HourlyItem(randomMockForecast, randomHour)
        }
        //Gets the temperature that should be displayed for the random hour
        val temperature = randomMockForecast.getHourlyData(randomHour)!!.temp

        //Finds Node which displays the HourlyItem temperature and asserts the correct time is displayed
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("Temp Unmerged")
        composeTestRule.onRoot().printToLog("Temp Merged")
        composeTestRule.onNodeWithText("${temperature}°").assertExists().assertIsDisplayed()
    }

}