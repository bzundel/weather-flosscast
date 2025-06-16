package de.frauas.weather_flosscast.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.CityList
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.getCitySearchResults
import de.frauas.weather_flosscast.getForecastFromCacheOrDownload
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate


/**
 * "SearchScreen" Composable that loads and shows all the contents
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onCitySelected: (String) -> Unit, navController: NavController) {
    // Declaring Values
    val context = LocalContext.current
    /*Remeber states*/
    var inputText by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var savedCities by remember { mutableStateOf(CityList.getCities(context).filter { it.cityName.contains(query, ignoreCase = true) }) }
    var forecasts by remember { mutableStateOf<Map<String, Forecast>>(emptyMap()) }
    var isRefreshing by remember { mutableStateOf(false) }               //Refreshing State for the refresh/swipeState. Std-Value false
    var isLoading by remember { mutableStateOf(true) }                   //Loading State for the initial load of the CityList, Std-Value true
    val scope = rememberCoroutineScope()
    val swipeState = rememberSwipeRefreshState(isRefreshing)            //State for the swipeAnimation
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
    /* BackHandler values */
    var lastBackPressedTime by remember { mutableStateOf(0L) }
    val backPressInterval = 1500L // 1,5 seconds
    val currentTime = System.currentTimeMillis()

        //Update Forescreens and cityList at the start
        isLoading = true                                                    //isLoading = true for shimmer-effect when loading the list
    LaunchedEffect(savedCities) {forecasts = loadForecastsForCities(context, savedCities, false)}     //Loading forecast without forcing  the update(Taking data from cache first)
        savedCities = CityList.getCities(context)                           //refresh cityList direct at the beginning of the function
        isLoading = false                                                   //isLoading = false to disable the shimmer-effect


    //BackHandler for resetting search field and closing the application
    BackHandler {
        if (inputText.isNotEmpty() || query.isNotEmpty()) {             // Reset state to "reload" SearchScreen
            navController.navigate("search") {
                popUpTo("search") { inclusive = true }}  //Loads Searchscreen one more time completely
        } else {                                                             //If the list is empty, only saving the click
            if (currentTime - lastBackPressedTime < backPressInterval) {     //If time between 2 clicks too low -> close application
                (context as? android.app.Activity)?.finish()    //Closing application
            } else {
                lastBackPressedTime = currentTime   //Time between 2 clicks too low -> Toast message and resetting state time
                Toast.makeText(context, "Noch einmal drücken zum Beenden", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Scattfold Layout Template
    Scaffold(
        topBar = {                                                                                  //App-Bar
            TopAppBar(
                title = { Text(text = "Wetter", color = Color.White, fontSize = 25.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        modifier = Modifier.background(Color.Black)
    ) { paddingValues ->
        SwipeRefresh(                                                                               //SwipeRefresh under the Search-Bar
            state = swipeState,
            onRefresh = {                                                                           //isRefreshing = true for a animation
                scope.launch {                                                                      //load all Forecasts with force boolean
                    isRefreshing = true
                    forecasts = loadForecastsForCities(context, savedCities,true)
                    savedCities = CityList.getCities(context)
                    ///Toast.makeText(context, "Daten aktualisiert", Toast.LENGTH_SHORT).show()     //Toast for debugging
                    delay(500)                                                                      //Show loading animation longer
                    isRefreshing = false                                                            //Animation ends isRefreshing = false
                }
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(                                                              //SwipeRefreshIndicator Values
                    state = state,                                                                  //state and colours
                    refreshTriggerDistance = trigger,
                    backgroundColor = Color.DarkGray,
                    contentColor = Color.White
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            if (isLoading) {                                       //ShimmerEffect
                Column(                                            //If weather Data still loading -> show shimmer effect first, then show results
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    repeat(5) {
                        Box(
                            modifier = Modifier                 //ShimmerEffect modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(vertical = 8.dp)
                                .shimmer(shimmerInstance)
                                .background(Color.DarkGray, shape = RoundedCornerShape(15.dp))
                        )
                    }
                }
            } else {
                LazyColumn(                                     //LazyColumn
                    modifier = Modifier                         //Scrollable list for the List with searched and old cities
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        TextField(                              //Textfield Search-Bar
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Stadt oder Flughafen suchen", color = Color.Gray) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {                    //after entering text -> save it and clear input
                                    query = inputText
                                    inputText = ""
                                }
                            ),
                            trailingIcon = {
                                IconButton(onClick = {          //Onclick on search-Icon -> save it and clear input
                                    query = inputText
                                    inputText = ""
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Search, //Search-Icon
                                        contentDescription = "Suchen",
                                        tint = Color.Black
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CityListView(                           //CityListview function call
                            context = context,
                            query = query,
                            onCitySelected = { cityName ->
                                val city = CityList.getCities(context).firstOrNull { it.cityName == cityName }
                                if (city != null) {
                                    CityList.addCity(context, city) //update the index of city if it exist on the list
                                }
                                onCitySelected(cityName)            //if CityCard clicked -> Jump to WeatherScreen with cityName String value
                            },
                            cities = savedCities,
                            forecasts = forecasts,
                            onCityDeleted = {
                                savedCities = CityList.getCities(context)
                            }
                        )
                   }
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
    onCitySelected: (String) -> Unit,
    onCityDeleted: () -> Unit
) {
    //Searched/new cities are first on the List
    var searchResults by remember { mutableStateOf<List<City>>(emptyList()) }
    //Makes the list empty, than add new ones
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            searchResults = emptyList()         //Empty list so that there are no old values
            try {
                val searchResultsRaw = getCitySearchResults(query)  /*getting raw data              Save only those cities which have the same name, state, country so there is no redundancy
                                                                                                    when searching cities (needs updates in the future, not perfect way to do it) */
                searchResults = searchResultsRaw.distinctBy { listOf(it.cityName, it.state, it.country) }
            } catch (e: Exception) {
                Toast.makeText(context, "Fehler beim Laden der Suche", Toast.LENGTH_SHORT).show()   //If the getCitySearchResults function im empty, throw Exception
                e.printStackTrace()
            }
        } else {
            searchResults = emptyList()                                                             //If query is empty, empty the list also
        }
    }
    // Print the result for city search                                                             //Print newCityCard for every City from CityList
    Column {
        searchResults.forEach { city ->         //Print each city once
            NewCityCard(context, city) {                                                            //NewCityCard for each city from list
                onCitySelected(city.cityName)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    var cityToDelete by remember { mutableStateOf<City?>(null) }                                    //DeleteDialog for CityCards
    if (cityToDelete != null) {                                                                     //if citytoDelete triggered -> remove City from the List and refresh
        AlertDialog(
            onDismissRequest = { cityToDelete = null },
            title            = { Text("${cityToDelete!!.cityName} wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        CityList.removeCity(context, cityToDelete!!)
                        onCityDeleted()         // fetch city list
                        cityToDelete = null },  //reset state
                    ) { Text("Löschen") } },
            dismissButton    = { TextButton(onClick = { cityToDelete = null }) { Text("Abbrechen") } }  //Dismiss Button
        )
    }
// Print the list of saved Cities
    Column {
        cities.forEach { city -> val forecast = forecasts[city.cityName]                            //update forecast for each city on the list(without force update)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            CityList.addCity(context, city)                                         //if clicked on CityCard -> Add city to list
                            onCitySelected(city.cityName)                                           //goto WeatherScreen on clicked city
                        },
                        onLongClick = { cityToDelete = city }
                    )
            ) {
                CityCard(city = city, forecast = forecast, modifier = Modifier)                      //CityCard for each city from CityList
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

}

/**
 * Composable which creates Cards for searched cities
 */
@Composable
fun NewCityCard(
    context: Context,
    city: City,
    onCitySelected: (String) -> Unit
) {
//Create a coroutine scope tied to the composables lifecycle
    val scope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                scope.launch {                                                                      //Launch coroutine to load forecast without blocking UI
                    try {
                        // Try to Load forecast
                        getForecastFromCacheOrDownload(                                             //Checking if forecast is even possible for our coordinates
                            context.filesDir,
                            city.latitude,
                            city.longitude
                        )

                        if (CityList.getCities(context).contains(city)) Toast.makeText(context, "Ort '${city.cityName}' existiert bereits.", Toast.LENGTH_SHORT).show()
                        CityList.addCity(context, city)               //  Add city to saved list
                        onCitySelected(city.cityName)                 //  Notify NavHost
                        Toast.makeText(context, "${city.cityName} hinzugefügt", Toast.LENGTH_SHORT)
                            .show()

                    } catch (e: Exception) {                                                        //If not, give user some Toast notification
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
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)                           //CardColor
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(
                text = city.cityName,                                                               //Cityname on the cards with new cities
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 20.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(3.dp))

            Text(
                text = city.state + ", " + city.country,                                            //State and country
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

/**
 * Composable which creates Cards for already savedCities
 */
@Composable
fun CityCard(
    city: City,
    forecast: Forecast?, // nullable
    modifier: Modifier = Modifier,

) {
    val wmoCode   = forecast?.getWmoCodeAndIsNight()?.first ?: 0                    //Gets newest weathercode from forecast
    val isNight = forecast?.getWmoCodeAndIsNight()?.second ?: false


    //Color for the backgrounds of the CityCard
    val bgColor = colorForWmoCode(wmoCode, isNight)
    val iconRes   = getIconForWmoCode(wmoCode, isNight)                                     //gets the right icon from function

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
                Text(
                    text = city.cityName,                       //Get the name of the city
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 23.sp,
                    maxLines = 1,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = city.state + ", " + city.country,    //Get the state + country
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.width(8.dp))
                                                                                                    //Get last updated forecast timestamp
                Row {
                    Text(
                        text = forecast?.timestamp?.let { ts ->
                            // 1) Datum formatieren
                            val date = ts.date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("dd.MM"))
                            // 2) Uhrzeit formatieren
                            val time = ts.time.toJavaLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                            // 3) beides zu einem String zusammenfügen
                            "$date, $time"
                        } ?: "--",  // falls forecast oder timestamp null ist
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            //Get current and min/max temperatures in a column
            Column (){
                Text(
                    text = "${forecast?.getCurrentTemperature() ?: "-"}" + "°",
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