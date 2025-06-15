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

//MAIN ACTIVITY WITH APPNAVHOST
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                //Checking, if the CityList is Empty
                if(CityList.getCities(this).isEmpty()){
                    //Toast.makeText(this, "Bitte zuerst ein Ort hinzufügen", Toast.LENGTH_SHORT).show()    //Toast for debugging

                    AppNavHost("search")                                                 //if empty, show SearchScreen
                }else{
                    AppNavHost("weather/${CityList.getCities(this).first().cityName}")  //if not empty, show WeatherScreen of the first City on the CityList
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
                onCitySelected = { city ->                      // wenn Karte angeklickt, navigiere zu WeatherScreen mit city als Argument
                    navController.navigate("weather/${city}"){
                        popUpTo("search"){inclusive = false}    //backstack-tracing is ON
                        launchSingleTop = false                  //if true, if search is on top, the NavController will not push a new instance onto the back-stack
                    }},
                    navController = navController               //passing Navcontroller to SearchScreen
            )
        }

        // 3. WeatherScreen-Route with the arg "city"
        composable(
            route = "weather/{cityName}",
            arguments = listOf(navArgument("cityName") {
                type = NavType.StringType
            })
        ) {                                                                                     //BackStackEntry is not used in SearchScreen
            backStackEntry -> val city = backStackEntry.arguments?.getString("cityName") ?: "" //extracting the cityName argument from it to pass to WeatherScreen.
            WeatherScreen(
                city, onBack = {          //Go back to Search-Screen without backstack-tracing further-on
                navController.navigate("search") {
                    popUpTo("weather/{cityName}") { inclusive = true }      //Backstack-tracing is OFF
                    launchSingleTop = true //prevents creating a duplicate of "search" on the back stack.
                }
            })
        }
    }
}