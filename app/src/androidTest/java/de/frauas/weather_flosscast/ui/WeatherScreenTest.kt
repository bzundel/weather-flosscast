package de.frauas.weather_flosscast.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.printToLog
import androidx.navigation.NavController
import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.generateMockForecast
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.format.DateTimeFormatter
import org.junit.Assert.*
import org.junit.Before
import kotlin.random.Random

class WeatherScreenTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	lateinit var randomMockForecast: Forecast
	var time: Int = 1

	@Before
	fun randomForecast() {
		randomMockForecast = generateMockForecast()
		time = randomMockForecast.timestamp.hour
	}

	lateinit var randomForecast: Forecast

	@Before
	fun createRandomForecast() {
		randomForecast = generateMockForecast()
	}

	@Test
	fun sevenDayForecast_hasRightHeading() {
		composeTestRule.setContent {
			SevenDayForecastBlock(randomForecast)
		}
		composeTestRule.onNodeWithText("7-Tage-Vorhersage").assertExists()
		composeTestRule.onRoot().printToLog("SevenDayForecast")
	}

	@Test
	fun sevenDayForecast_hasSevenItems() {
		composeTestRule.setContent {
			SevenDayForecastBlock(randomForecast)
		}

		composeTestRule.onAllNodesWithText("%", substring = true).assertCountEquals(7)
		composeTestRule.onRoot().printToLog("SevenDayForecast")
		composeTestRule.onRoot(useUnmergedTree = true).printToLog("SevenDayForecast unmerged")
		Thread.sleep(5000)
	}

	@Test
	fun dailyItem_hasAllItems() {
		composeTestRule.setContent {
			DailyItem(randomForecast, Random.nextInt(1,7))
		}

		composeTestRule.onNode(hasTestTag("day")).assertIsDisplayed()
		composeTestRule.onNodeWithText("%", substring = true).assertIsDisplayed()
		composeTestRule.onAllNodesWithText("°", substring = true).assertCountEquals(2)
		composeTestRule.onAllNodesWithTag("Image").assertCountEquals(4)
		composeTestRule.onRoot().printToLog("DailyItem")
		composeTestRule.onRoot(useUnmergedTree = true).printToLog("DailyItem unmerged")
		Thread.sleep(5000)
	}

}
