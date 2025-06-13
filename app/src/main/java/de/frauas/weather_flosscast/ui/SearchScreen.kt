package de.frauas.weather_flosscast.ui

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import de.frauas.weather_flosscast.R
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
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
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

//function for select the card-color
private fun colorForCondition(condition: String): Color {
    return when (condition.lowercase()) {
        "regen", "regnerisch", "niesel" -> Color(0xFF808080)   // Dunkelgrau
        "sonnig", "klar", "heiter"        -> Color(0xFF33AAFF)   // Blau
        "schnee", "schneeschauer"         -> Color(0xFFB0BEC5)   // Hellgrau
        else                               -> Color(0xFF546E7A)   //
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onCitySelected: (String) -> Unit,
) {
    val context = LocalContext.current  // Safe save for app context for Compose
    var inputText by remember { mutableStateOf("") }    //updates directly
    var query by remember { mutableStateOf("") } // updates only after clicking

    //Layout
    Scaffold(
        topBar = {      //heading "Wetter"
            TopAppBar(
                title = {
                    Text(
                        text = "Wetter",
                        color = Color.White,
                        fontSize = 25.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        modifier = Modifier.background(Color.Black)  // background = schwarz
    ) {
        paddingValues ->
    //Layout searchfield + list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
            // 1) searchfield with icon-button to search
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Stadt oder Flughafen suchen", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),

                // Zeigt "Suchen" auf der Tastatur
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),

                // Reagiert auf "Suchen"-Taste (Enter)
                keyboardActions = KeyboardActions(
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
            //Space between searchbar and list
            Spacer(modifier = Modifier.height(16.dp))

            //2) Drawing the list with cities
            CityListView(context = context, query = query, onCitySelected = onCitySelected)
            }
        }
    }
}


@Composable
fun CityListView(
    context: Context,
    query: String,
    onCitySelected: (String) -> Unit
) {
    //1. Show searched/new cities
    var searchResults by remember { mutableStateOf<List<City>>(emptyList()) }

    // Leere die Liste zuerst, dann lade neue Ergebnisse
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            searchResults = emptyList() // Liste vor dem Laden leeren
            try {
                val cities = getCitySearchResults(query)
                searchResults = cities


                /*// API-Test
                cities.forEach { city ->
                    Toast.makeText(context, "'${city.cityName}'", Toast.LENGTH_SHORT).show()
                }*/
            } catch (e: Exception) {
                Toast.makeText(context, "Fehler beim Laden der Suche", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } else {
            searchResults = emptyList() // Wenn leerer Suchbegriff, ebenfalls leeren
        }
    }

    Column {
        searchResults.forEach { city ->
            NewCityCard(context = context, city = city) {
                onCitySelected(city.cityName)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    //2. Show saved cities
    val appDir = context.filesDir
    val savedCities = CityList.getCities(context).filter { it.cityName.contains(query, ignoreCase = true) }
    var forecasts by remember { mutableStateOf<Map<String, Forecast>>(emptyMap()) }

    LaunchedEffect(savedCities) {
        val result = mutableMapOf<String, Forecast>()
        for (city in savedCities) {
            try {
                val forecast = getForecastFromCacheOrDownload(appDir, city.latitude, city.longitude)
                result[city.cityName] = forecast
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        forecasts = result
    }

    Column {
        savedCities.forEach { city ->
            val forecast = forecasts[city.cityName]
            CityCard(city = city, forecast = forecast) {
                onCitySelected(city.cityName)
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
    Card(
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                CityList.addCity(context, city)               //  Add city to saved list
                onCitySelected(city.cityName)                 //  Notify NavHost
                Toast.makeText(context, "${city.cityName} hinzugefügt", Toast.LENGTH_SHORT).show()
            },
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(16.dp)
        ) {
            Text(
                text = city.cityName,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = city.state + ", " + city.country,
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
fun CityCard(    city: City,
                 forecast: Forecast?, // nullable
                 onClick: () -> Unit
) {
    // Hintergrundfarbe je nach Wetterbedingung
    val bgColor = colorForCondition("regen"/*city.condition*/)

    Card(
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        // all content in a row
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            //Icon of current condition
            val iconRes = when ("regen"/*city.condition.lowercase()*/) {
                "sonnig", "klar", "heiter"       -> R.drawable.sun
                "regen", "regnerisch", "niesel"  -> R.drawable.rain
                "schnee", "schneeschauer"        -> R.drawable.snow
                else                              -> null
            }

            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "regen"/*city.condition*/,
                    modifier = Modifier.size(40.dp)
                )
            } else { Spacer(modifier = Modifier.width(40.dp)) }

            Spacer(modifier = Modifier.width(24.dp))

            // cityname and co. in a column
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f)

            ) {
                    //Cityname
                    Text(
                        text = city.cityName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 22.sp,
                        maxLines = 1,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    //last update was when
                    Text(
                        //text = "${forecast?.timestamp?.time?.hour ?: "--"}:${forecast?.timestamp?.time?.minute ?: "--"}",
                        text = "last updated: " + forecast?.timestamp?.time?.toJavaLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),

                    )
                //city current weather condition
                Text(
                    text = "regen"/*city.condition*/,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    fontSize = 14.sp
                )
            }

            //temperatures in a column
            Column (){
                //current temperature
                Text(
                    //text = "${forecast?.days?.firstOrNull()?.hourlyValues?.firstOrNull()?.temperature?.roundToInt() ?: "--"}°", //Live temp now
                    //text = "${forecast?.days?.getOrNull(1)?.hourlyValues?.getOrNull(getTempForHour(forecast, ))?.temperature?.roundToInt() ?: "--"}°", //Live temp now
                    text = "${forecast?.getCurrentTemperature()}" + "°",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    color = Color.White
                )
                //Space
                Spacer(modifier = Modifier.width(18.dp))

                //High and low temperatures under
                Text(

                    /*text = "${forecast?.days?.!!!firstOrNull()!!!?.hourlyValues?.maxOfOrNull { it.temperature }?.roundToInt() ?: "--"}°" +//MIN Max only for last day and last hour, so it is wrong!
                            "/${forecast?.days?.!!!firstOrNull()?!!!.hourlyValues?.minOfOrNull { it.temperature }?.roundToInt() ?: "--"}°",*/
                    text = "${getDailyMinTemp(forecast).roundToInt() ?: "--"}°"
                    + "/${getDailyMaxTemp(forecast).roundToInt() ?: "--"}°",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                )
            }
        }
    }
}

fun Forecast.getCurrentTemperature(): Int? {//Utility function to update newest temperature
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val today = now.date
    val currentHour = now.hour

    val todayForecast = this.days.firstOrNull { it.date == today } ?: return null

    return todayForecast.hourlyValues.firstOrNull { it.dateTime.hour == currentHour }?.temperature?.roundToInt()
}
fun getDailyMinTemp(forecast: Forecast?): Double {
    if (forecast == null) return 0.0

    val allTemps = mutableListOf<Double>()

    for (daily in forecast.days) {
        // add all temps to the list
        allTemps.addAll(daily.hourlyValues.map { it.temperature })
    }
    return allTemps.minOrNull() ?: 0.0
}
fun getDailyMaxTemp(forecast: Forecast?): Double {
    if (forecast == null) return 0.0

    val allTemps = mutableListOf<Double>()

    for (daily in forecast.days) {
        // add all temps to the list
        allTemps.addAll(daily.hourlyValues.map { it.temperature })
    }
    return allTemps.maxOrNull() ?: 0.0
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(onCitySelected = { query ->
            // Beispiel-Callback:
            // Hier könntest du navigieren, Toast anzeigen, Liste neu laden...
            println("Suchtext abgeschickt: $query")
        })
}
