package de.frauas.weather_flosscast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import de.frauas.weather_flosscast.ui.SearchScreen
import de.frauas.weather_flosscast.ui.WeatherScreen
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.CompositionLocalProvider



//MAIN ACTIVITY WITH APPNAVHOST
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NoFontScaling {//Deactivate font scaling of mobile-phone
                    AppNavHost(
                        startCity = CityList.getCities(this).firstOrNull()?.cityName
                    )
                }
            }
        }
    }
}
@Composable
fun AppNavHost(startCity: String?) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        // Not visible start point -> shows the next direction Searchscreen or Weather Screen
        composable("start") {
            LaunchedEffect(Unit) {
                if (startCity != null) {        //If there are cities on the List, put Search Screen first in the backstack and go to the city then
                    navController.navigate("search") {
                        launchSingleTop = true
                    }
                    navController.navigate("weather/$startCity") {
                        launchSingleTop = true
                    }
                } else {    //if the list is emty, just go to the search screen
                    navController.navigate("search") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            }
        }

        composable("search") {  //SearchScreen navigation. If there is a city Select, go to WeatherScreen with City Name
            SearchScreen(
                onCitySelected = { city ->
                    navController.navigate("weather/$city") {
                        popUpTo("search") { inclusive = false }
                    }
                },
                navController = navController
            )
        }

        composable(     //WeatherScreeen navigation. Take CityName and open WeatherScreen with it. Go to the SearchScreen on back click.
            "weather/{cityName}",
            arguments = listOf(navArgument("cityName") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val city = backStackEntry.arguments?.getString("cityName") ?: ""
            WeatherScreen(
                city,
                onBack = {
                    navController.popBackStack("search", inclusive = false)
                }
            )
        }
    }
}
//Deactivate font scaling of mobile-phone//
@Composable
fun NoFontScaling(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density.density,
            fontScale = 1f
        )
    ) {
        content()
    }
}