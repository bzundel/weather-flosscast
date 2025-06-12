package de.frauas.weather_flosscast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import de.frauas.weather_flosscast.ui.SearchScreen
import de.frauas.weather_flosscast.ui.WeatherScreen
import androidx.compose.runtime.*
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

                if(CityList.getCities(this).isEmpty()){
                    Toast.makeText(this, "Bitte zuerst ein Ort hinzufügen", Toast.LENGTH_SHORT).show()
                    AppNavHost("search")
                }else{
                    AppNavHost("weather/${CityList.getCities(this).first().cityName}")
                }
            }
        }
    }
}
@Composable
fun AppNavHost(startDestination: String) {
    // 1) Erzeuge einen NavController
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination    // Startscreen
    ) {
        // 2) SearchScreen-Route
        composable("search") {
            SearchScreen(
                onCitySelected = { city ->
                    // wenn Karte angeklickt, navigiere zu WeatherScreen mit city als Argument
                    navController.navigate("weather/${city}")
                }
            )
        }
        // 3) WeatherScreen-Route mit Argument "city"
        composable(
            route = "weather/{cityName}",
            arguments = listOf(navArgument("cityName") {
                type = NavType.StringType
            })
        ) { backStackEntry -> val city = backStackEntry.arguments?.getString("cityName") ?: ""
            WeatherScreen(
                city,
                onBack   = { navController.navigate("search") }  // zurück zur SearchScreen
            )
        }
    }
}