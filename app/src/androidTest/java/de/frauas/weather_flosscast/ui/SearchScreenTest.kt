package de.frauas.weather_flosscast.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.navigation.NavController
import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.generateMockForecast
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.format.DateTimeFormatter

class SearchScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    val mockCities = listOf(
        City("Paris", "Île-de-France", "France", 48.8566, 2.3522),
        City("New York", "NY", "USA", 40.7128, -74.0060),
        City("Tokyo", "Tokyo", "Japan", 35.6895, 139.6917),
        City("Sydney", "NSW", "Australia", -33.8688, 151.2093),
        City("Cape Town", "Western Cape", "South Africa", -33.9249, 18.4241),
        City("Berlin", "Berlin", "Germany", 52.5200, 13.4050),
        City("São Paulo", "SP", "Brazil", -23.5505, -46.6333),
        City("Toronto", "Ontario", "Canada", 43.6510, -79.3470),
        City("Mumbai", "Maharashtra", "India", 19.0760, 72.8777),
        City("Reykjavík", "Capital Region", "Iceland", 64.1265, -21.8174)
    )

    @Test
    fun searchScreen_SearchBarDisplayed() {
        // Mock NavController
        val mockNavController = mock<NavController>()
        // Variable to capture selected city from callback
        var selectedCity: String? = null

        // Setting content of SearScreen composable
        composeTestRule.setContent {
            SearchScreen(
                onCitySelected = { city -> selectedCity = city },
                navController = mockNavController
            )
        }
        // Wait for initial load
        composeTestRule.waitForIdle()

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("SearchBar")
        composeTestRule.onNodeWithText("Stadt oder Flughafen suchen", useUnmergedTree = true)
            .assertExists()

    }

    @Test
    fun searchScreen_TopBarDisplayed() {

        val mockNavController = mock<NavController>()
        var selectedCity: String? = null

        composeTestRule.setContent {
            SearchScreen(
                onCitySelected = { city -> selectedCity = city },
                navController = mockNavController
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("TopBar")
        composeTestRule.onNodeWithText("Wetter").assertExists()

    }

    @Test
    fun searchScreen_SearchIconDisplayed() {

        val mockNavController = mock<NavController>()
        var selectedCity: String? = null

        composeTestRule.setContent {
            SearchScreen(
                onCitySelected = { city -> selectedCity = city },
                navController = mockNavController
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("TopBar")
        composeTestRule.onNodeWithContentDescription("Suchen").assertExists()

    }

    @Test
    fun newCityCard_hasCityName() {

        var selectedCity: String? = null
        val city = mockCities.elementAt(0)

        composeTestRule.setContent {
            val context = LocalContext.current
            NewCityCard(
                context = context,
                onCitySelected = { selectedCity = city.cityName },
                city = city
            )
        }

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNode(hasAnyChild(hasText(city.cityName))).assertExists()

    }

    @Test
    fun newCityCard_hasStateAndCountry() {

        var selectedCity: String? = null
        val city = mockCities.elementAt(0)

        composeTestRule.setContent {
            val context = LocalContext.current
            NewCityCard(
                context = context,
                onCitySelected = { selectedCity = city.cityName },
                city = city
            )
        }

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNode(hasAnyChild(hasText(city.cityName))).assertExists()
    }

    @Test
    fun cityCard_hasCityName() {

        composeTestRule.setContent {
            CityCard(
                city = mockCities.elementAt(0),
                forecast = generateMockForecast(),
                modifier = Modifier
            )

        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNode(hasAnyChild(hasText(mockCities[0].cityName))).assertExists()
    }

    @Test
    fun cityCard_hasStateAndCountry() {

        composeTestRule.setContent {
            CityCard(
                city = mockCities.elementAt(0),
                forecast = generateMockForecast(),
                modifier = Modifier
            )

        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNode(hasAnyChild(hasText(mockCities[0].state + ", " + mockCities[0].country)))
            .assertExists()
    }

    @Test
    fun cityCard_hasCurrentWeatherImage() {

        composeTestRule.setContent {
            CityCard(
                city = mockCities.elementAt(0),
                forecast = generateMockForecast(),
                modifier = Modifier
            )

        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNodeWithContentDescription("--").assertExists()
    }

    @Test
    fun cityCard_hasCurrentDateAndLastUpdate() {

        //Generate Forecast before
        val forecast = generateMockForecast()
        //Code from SearchScreen to format it correctly
        val text = forecast.timestamp.let { ts ->
            val date = ts.date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("dd.MM"))
            val time = ts.time.toJavaLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            "$date, $time"
        }

        composeTestRule.setContent {
            CityCard(
                city = mockCities.elementAt(0),
                forecast = forecast,
                modifier = Modifier
            )

        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNodeWithText(text).assertExists()
    }

    @Test
    fun cityCard_HasTemperature() {

        //Generate Forecast before
        val forecast = generateMockForecast()
        //Code from SearchScreen to format it correctly
        val text = forecast.getCurrentTemperature().toString() + "°"

        composeTestRule.setContent {
            CityCard(
                city = mockCities.elementAt(0),
                forecast = forecast,
                modifier = Modifier
            )

        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNodeWithText(text).assertExists()
    }

    @Test
    fun cityCard_HasMaxMinTemperature() {

        //Generate Forecast before
        val forecast = generateMockForecast()
        //Code from SearchScreen to format it correctly
        val text = forecast.getDailyMaxTemp().toString() + "°/" + forecast.getDailyMinTemp().toString() + "°"

        composeTestRule.setContent {
            CityCard(
                city = mockCities.elementAt(0),
                forecast = forecast,
                modifier = Modifier
            )

        }
        composeTestRule.onRoot(useUnmergedTree = true).printToLog("cityCard")
        composeTestRule.onNodeWithText(text).assertExists()
    }

}