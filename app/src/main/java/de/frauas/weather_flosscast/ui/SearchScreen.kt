package de.frauas.weather_flosscast.ui

import android.content.Context
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import de.frauas.weather_flosscast.R
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.CityList
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.getCitySearchResults
import de.frauas.weather_flosscast.getForecastFromCacheOrDownload
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onCitySelected: (String) -> Unit,) {

    val context = LocalContext.current  // Safe save for app context for Compose
    var inputText by remember { mutableStateOf("") }    //updates directly
    var query by remember { mutableStateOf("") } // updates only after clicking

    var isRefreshing by remember { mutableStateOf(false) }// Pull-to-Refresh state
    val scope      = rememberCoroutineScope()
    val swipeState = rememberSwipeRefreshState(isRefreshing)

    // List of saved citys
    val savedCities = CityList.getCities(context).filter { it.cityName.contains(query, ignoreCase = true) }
    // Map of forecasts by citynames
    var forecasts by remember { mutableStateOf<Map<String, Forecast>>(emptyMap()) }

    // Initial load the forecasts//
    LaunchedEffect(savedCities) { forecasts = loadForecastsForCities(context, savedCities) }


    /**
     * Layout + in topBar: Heading "Wetter"
     */
    Scaffold(//Scaffold is a basic structure for building user interfaces
        topBar = {
            TopAppBar(
                title = { Text(text = "Wetter", color = Color.White, fontSize = 25.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        modifier = Modifier.background(Color.Black)  // background = black
    ) {
        paddingValues->

        // SwipeRefresh component over the rest of the components
        // It refreshed the forecast-data when swiping down with indicator
        SwipeRefresh(
            state     = swipeState,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    forecasts = loadForecastsForCities(context, savedCities)
                    Toast.makeText(context, "Daten aktualisiert", Toast.LENGTH_SHORT).show()
                    delay(1000)
                    isRefreshing = false
                }
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state                   = state,
                    refreshTriggerDistance  = trigger,
                    backgroundColor         = Color.DarkGray,
                    contentColor            = Color.White
                )
            },
            modifier = Modifier.fillMaxSize().background(Color.Black).padding(paddingValues)
        ) {
            // Column for all components: searchbar and list of citys
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color.Black).padding(horizontal = 16.dp)
            ) {
                item {
                    //1) Searchfield with icon-button
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Stadt oder Flughafen suchen", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(// reacts to enter
                            onSearch = {
                                query = inputText
                                inputText = ""
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                query = inputText //reacts on search button too
                                inputText = "" //Empties search field

                            }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Suchen",
                                    tint = Color.Black
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    //2) Drawing the city-lists
                    CityListView(context = context, query = query, onCitySelected = onCitySelected,cities = savedCities, forecasts=forecasts)
                }
            }
        }
    }
}


/**
 * Composable that draws the components of NewCityCards (if the user searched a city) with the results for a city-search and the CityCards for the saved citys
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CityListView(
    context: Context,
    query: String,
    cities: List<City>,
    forecasts: Map<String, Forecast>,
    onCitySelected: (String) -> Unit
) {
    //1. Show searched/new cities
    var searchResults by remember { mutableStateOf<List<City>>(emptyList()) }
    // emptys the list, than adds the new ones
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            searchResults = emptyList() // Liste vor dem Laden leeren
            try {
                val cities = getCitySearchResults(query)
                //Save only those cities which have same name, state, country so there is no redundancy
                // is being shown when searching cities (needs updates in the future, not perfect way to do it)
                searchResults = cities.distinctBy { listOf(it.cityName, it.state, it.country) }
            } catch (e: Exception) {
                Toast.makeText(context, "Fehler beim Laden der Suche", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            searchResults = emptyList() // Wenn leerer Suchbegriff, ebenfalls leeren
        }
    }
    // Print the result for city search
    Column {
        searchResults.forEach { city ->
            NewCityCard(context, city) {
                onCitySelected(city.cityName)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    var cityToDelete by remember { mutableStateOf<City?>(null) }

    //For longclick if value not empty them show alertdialog
    if (cityToDelete != null) {
        AlertDialog(
            onDismissRequest = { cityToDelete = null },
            title            = { Text("${cityToDelete!!.cityName} wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        CityList.removeCity(context, cityToDelete!!)
                        cityToDelete = null },
                    ) { Text("Löschen") } },
            dismissButton    = { TextButton(onClick = { cityToDelete = null }) { Text("Abbrechen") } }
        )
    }

// Print the list of saved citys
    Column {
        cities.forEach { city -> val forecast = forecasts[city.cityName]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onCitySelected(city.cityName) },
                        onLongClick = { cityToDelete = city }
                    )
            ) {
                CityCard(context, city = city, forecast = forecast, modifier = Modifier)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

}

//Cards for searched cities
@Composable
fun NewCityCard(
    context: Context,
    city: City,
    onCitySelected: (String) -> Unit
) {
    // Coroutine-Scope für den Klick
    val scope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                scope.launch {
                    try{
                        // 1) Versuch, Forecast zu laden
                        getForecastFromCacheOrDownload(
                            context.filesDir,
                            city.latitude,
                            city.longitude
                        )

                        // 2) wenn erfolgreich, zur Liste hinzufügen und navigieren
                        if (CityList.getCities(context).contains(city)) Toast.makeText(
                            context,
                            "Ort '${city.cityName}' existiert bereits.",
                            Toast.LENGTH_SHORT
                        ).show()
                        CityList.addCity(context, city)               //  Add city to saved list
                        onCitySelected(city.cityName)                 //  Notify NavHost
                        Toast.makeText(context, "${city.cityName} hinzugefügt", Toast.LENGTH_SHORT).show()

                    } catch (e: Exception) {
                        // 3) wenn fehlschlägt, nur Toast
                        Toast
                            .makeText(
                                context,
                                "Für ${city.cityName} konnten keine Wetterdaten geladen werden",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
            }
            },
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(16.dp)
        ) {
            Text(
                text = city.cityName,   //Cityname on the cards with new cities
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = city.state + ", " + city.country,    //more data like the state and country
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

//Cards for the citys
@Composable
fun CityCard(
    context: Context,
    city: City,
    forecast: Forecast?, // nullable
    modifier: Modifier = Modifier,

) {

    // Prüfung isNight?//
    val today     = forecast?.days?.firstOrNull()
    val rawSunrise= today?.sunrise
    val rawSunset = today?.sunset
    val sunriseLdt = rawSunrise
        ?.toString()
        ?.let { LocalDateTime.parse(it) }
    val sunsetLdt  = rawSunset
        ?.toString()
        ?.let { LocalDateTime.parse(it) }
    val now        = LocalDateTime.now()
    val isNight    = if (sunriseLdt != null && sunsetLdt != null) {
        now.isBefore(sunriseLdt) || now.isAfter(sunsetLdt)
    } else false


    // Hintergrundfarbe je nach Wetterbedingung
    val bgColor = colorForCityCard(forecast?.days?.firstOrNull()?.hourlyValues?.firstOrNull()?.weatherCode ?: 0, isNight )

    Card(
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),

        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        // all content in a row
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){

            val wmoCode   = today?.hourlyValues?.firstOrNull()?.weatherCode ?: 0    //gets weathercode
            val iconRes   = getIconForWmoCode(wmoCode, isNight)                     //gets the right icon

            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "--",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(26.dp))

            // cityname and co. in a column
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f)

            ) {
                // Print cityname
                Text(
                    text = city.cityName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 23.sp,
                    maxLines = 1,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))

                //Print state + country
                Text(
                    text = city.state + ", " + city.country,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.width(8.dp))

                //Print last updated date
                Row {
                    Text(
                        text = forecast?.timestamp?.let { ts ->
                            // 1) Datum formatieren
                            val date = ts.date
                                .toJavaLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd.MM"))
                            // 2) Uhrzeit formatieren
                            val time = ts.time
                                .toJavaLocalTime()
                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                            // 3) beides zu einem String zusammenfügen
                            "$date, $time"
                        } ?: "--",  // falls forecast oder timestamp null ist
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            //temperatures in a column
            Column (){
                //current temperature
                Text(
                    text = "${forecast?.getCurrentTemperature()}" + "°",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 25.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(18.dp))

                //High and low temperatures under
                Text(
                    text = "${forecast?.getDailyMaxTemp()}°" + "/${forecast?.getDailyMinTemp()}°",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Function to get the current temperature
 */
fun Forecast.getCurrentTemperature(): Int? {//Utility function to update newest temperature
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.date
    val currentHour = now.hour

    val todayForecast = this.days.firstOrNull { it.date == today } ?: return 0

    return todayForecast.hourlyValues.firstOrNull { it.dateTime.hour == currentHour }?.temperature?.roundToInt()
}
/**
 * Function to get the daily min temperature
 */
fun Forecast.getDailyMinTemp(): Int {

    val allTemps = mutableListOf<Double>()

    for (daily in days) {
        // add all temps to the list
        allTemps.addAll(daily.hourlyValues.map { it.temperature })
    }
    return allTemps.minOrNull()?.roundToInt() ?: 0
}
/**
 * Function to get the daily max temperature
 */
fun Forecast.getDailyMaxTemp(): Int {

    val allTemps = mutableListOf<Double>()

    for (daily in days) {
        // add all temps to the list
        allTemps.addAll(daily.hourlyValues.map { it.temperature })
    }
    return allTemps.maxOrNull()?.roundToInt() ?: 0
}


/**
 * Function for get the right color of the seperate city-cards
 */
private fun colorForCityCard(code: Int, isNight: Boolean): Color {
    return if (isNight) {
        // Color for nighttime:
        Color(0xFF37474F)// Very dark grayish blue
    } else {
        // Colors for daytime:
        // 0xFF33AAFF -> Vivid blue for clear sky
        // 0xFF808080 -> Dark gray for rain or storm
        // 0xFFB0BEC5 -> Grayish blue for snow or clouds
        when (code) {
            0                                                                         -> Color(0xFF33AAFF) // Vivid blue
            in 1..9, in 10..19, in 30..49,in 70..79            -> Color(0xFFB0BEC5) // Grayish blue
            in 20..29, in 50..59, in 60..69, in 80..99         -> Color(0xFF808080) // Dark gray
            else                                                                      -> Color(0xFFB0BEC5) // Fallback Grayish blue
        }
    }
}

/**
* Function for get the right icon based on weather-code and if its night or not
*/
@DrawableRes
fun getIconForWmoCode(code: Int, isNight: Boolean): Int {
    return if (isNight) {
        when (code) {
            0                                  -> R.drawable.monn          // klarer Himmel
            in 1..3                      -> R.drawable.cloud_moon    // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.drawable.storm         // Gewitter/Trichterwolke
            in 23..24, 26                -> R.drawable.snowrain      // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.drawable.snow          // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.drawable.rain          // Drizzle / Rain / Showers
            else                               -> R.drawable.cloud_moon    // Nebel, Staub, sonstige Wolken
        }
    } else {
        when (code) {
            0                                  -> R.drawable.sun           // klarer Himmel
            in 1..3                      -> R.drawable.cloud_sun     // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.drawable.storm         // Gewitter/Trichterwolke
            in 23..24, 26                -> R.drawable.snowrain      // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.drawable.snow          // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.drawable.rain          // Drizzle / Rain / Showers
            else                               -> R.drawable.cloud         // Nebel, Staub, sonstige Wolken
        }
    }
}


/**
 * Loads for all `cities` the forecast out of cache or download and gives a map back with the key cityName.
 */
suspend fun loadForecastsForCities(
    context: Context,
    cities: List<City>
): Map<String, Forecast> {
    val appDir = context.filesDir
    val result = mutableMapOf<String, Forecast>()
    for (city in cities) {
        try {
            val fc = getForecastFromCacheOrDownload(appDir, city.latitude, city.longitude)
            result[city.cityName] = fc
        } catch (_: Exception) {
            // Fehler pro Stadt ignorieren
        }
    }
    return result
}
