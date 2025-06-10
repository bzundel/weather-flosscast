package de.frauas.weather_flosscast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import de.frauas.weather_flosscast.ui.SearchScreen
import de.frauas.weather_flosscast.ui.WeatherScreen
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

//-------------------------------------------
//for testing the weather-screen
//-------------------------------------------
/*class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WeatherScreen(
                    cityName = "Frankfurt",
                    onBack   = { finish() }
                )
                }
            }
        }
    }*/


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AppNavHost()
            }
        }
    }
}
@Composable
fun AppNavHost() {
    // 1) Erzeuge einen NavController
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "search"    // Startscreen
    ) {
        // 2) SearchScreen-Route
        composable("search") {
            SearchScreen(
                onCitySelected = { city ->
                    // wenn Karte angeklickt, navigiere zu WeatherScreen mit city als Argument
                    navController.navigate("weather/${city}")
                },
                onSearch = { query ->
                    // wenn Lupe oder IME-Search
                    navController.navigate("weather/${query}")
                }
            )
        }
        // 3) WeatherScreen-Route mit Argument "city"
        composable(
            route = "weather/{cityName}",
            arguments = listOf(navArgument("cityName") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            // Extrahiere das Argument
            val city = backStackEntry.arguments?.getString("cityName") ?: ""
            WeatherScreen(
                cityName = city,
                onBack   = { navController.popBackStack() }  // zurück zur SearchScreen
            )
        }
    }
}