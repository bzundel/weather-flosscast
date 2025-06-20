package de.frauas.weather_flosscast.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.frauas.weather_flosscast.R
import com.airbnb.lottie.compose.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import de.frauas.weather_flosscast.CityList
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.getForecastFromCacheOrDownload
import de.frauas.weather_flosscast.ui.theme.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(cityName : String, onBack: () -> Unit) {
    val context = LocalContext.current                                      // Safe save for app context for Compose
    val city = CityList.getCities(context).find { it.cityName == cityName } //Getting cityData from CityList with find function
    var forecast by remember { mutableStateOf<Forecast?>(null) }            //Stores the forecast state and recomposition when updated
    var showAlert by remember { mutableStateOf(false) }                    //remember state for a Dialog

    var isRefreshing by remember { mutableStateOf(false) }                  // Pull-to-Refresh state
    val scope      = rememberCoroutineScope()
    val swipeState = rememberSwipeRefreshState(isRefreshing)

    //Update forecasts at the start of the App!
    LaunchedEffect(cityName) {
        forecast = loadForecastForOneCity(cityName, city, context, onBack).Forecast    //Updates forecasts or gets old cache data
        showAlert = loadForecastForOneCity(cityName, city, context, onBack).error
    }
    //If the forecast has any errors
    if (showAlert) {    //AlertDialog for old Forecast data -> better User Experience
        AlertDialog(
            onDismissRequest = { showAlert = false },
            confirmButton = {
                TextButton( onClick = { showAlert = false }) {
                    Text("OK")
                }
            },
            title = { Text("Warnung") },
            text = { Text("Daten veraltet!\nBitte Internetverbindung prüfen") }
        )
    }

    val currentForecast: Forecast = forecast ?: return          // load current forecast in variable
    val (wmoCode, isNight) = forecast!!.getWmoCodeAndIsNight()  //gets wmo-code and boolean for night-check
    val bgC = colorForWmoCode(wmoCode, isNight)                 //sets the background color for the whole screen

    // SwipeRefresh component over the rest of the components, It refreshed the forecast-data when swiping down with indicator
    SwipeRefresh(state = swipeState, onRefresh = {
        scope.launch {
            isRefreshing = true

            try {
                // Trying to load new Forecast
                val newForecast = city?.let {getForecastFromCacheOrDownload(context.filesDir,it.latitude,it.longitude,true)}
                    ?: throw IllegalArgumentException("City darf nicht null sein") //if city-val is empty throw a toast

                // update forecast values
                forecast = newForecast
                //Toast.makeText(context, "Daten aktualisiert", Toast.LENGTH_SHORT).show()  //Toast for debugging

            } catch (e: IOException) {  //Catch IOException if there are problems with network
                // If there is a network error, throw a toast
                Toast.makeText(context, "Keine Internetverbindung", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {    //If there are some IO and different errors
                // Throw a toast also
                Toast.makeText(context, "Fehler beim Laden", Toast.LENGTH_SHORT).show()
                Log.e("RefreshError", "Fehler beim Aktualisieren: ${e.localizedMessage}", e)
            } finally {
                // stop refresh-spinner
                delay(1000)
                isRefreshing = false
            }
        }
        },
        indicator = { state, trigger -> SwipeRefreshIndicator(
                state                   = state,
                refreshTriggerDistance  = trigger,
                backgroundColor         = Color.DarkGray,
                contentColor            = Color.White,
            )
        },
    ) {
        // Column of whole content
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(bgC),
            contentPadding = PaddingValues(vertical = 50.dp),//Space between top/bottom and content
            verticalArrangement = Arrangement.spacedBy(30.dp)//Space between the items
        ) {
            item {// 1) Header with Lottie-animation
                WeatherHeader(cityName, currentForecast, onBack)
            }
            item {// 2) Hourly forecast
                HourlyForecastRow(forecast)
            }
            item {// 3) Daily forecast
                SevenDayForecastBlock(forecast)
            }
            item {// 4) Info-boxes
                InfoBoxesSection(forecast)
            }
        }
    }
}

// -----------------------------------------------------------------------------
//Header: animation,city,temperature,condition
// -----------------------------------------------------------------------------
@Composable
 fun WeatherHeader(cityName : String, forecast: Forecast, onBack:   () -> Unit) {

     //  Select the right lottie-animation with data from getWMOCode function
     val(wmoCode, isNight) = forecast.getWmoCodeAndIsNight()
     val lottieRes = getLottieResForWmoCode(wmoCode, isNight)

     // Load the wmo-code and the condition-text
     val code      = forecast?.days?.firstOrNull()?.hourlyValues?.firstOrNull()?.weatherCode ?: 0
     val condition = getConditionForWmoCode(code)

    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {//content packed in a box with height of 200 dp
        Row(modifier = Modifier.fillMaxSize().padding(start = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {

            // 1) Lottie-animation
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(100.dp))

            // 2) Column with city,temperature,condition
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()  ) {

                // City-name with clickable function
                Text(
                    text = cityName,
                    style = MaterialTheme.typography.cityHeader,
                    modifier = Modifier
                        .clickable { onBack()} ,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Temperature
                Text(
                    text = "${forecast.getCurrentTemperature()}" + "°",
                    style = MaterialTheme.typography.temperatureHeader
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Condition
                Text(
                    text = condition,
                    style = MaterialTheme.typography.conditionHeader
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Hourly Items in a row in a darkened box
// -----------------------------------------------------------------------------
@Composable
 fun HourlyForecastRow(forecast : Forecast?) {
    Box(modifier = Modifier.padding(horizontal = 16.dp)) {//Space of box left and right

        // Overlay darkened box with max width and rounded corners
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().clip(RoundedCornerShape(12.dp))) {
            Box(
                modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f))
            )
            LazyRow(modifier = Modifier.fillMaxSize().padding(vertical = 15.dp)) {
                items((0 until 24).toList()) { hour ->  //creates 24 hourly items
                    HourlyItem(forecast, hour = hour)
                    Spacer(modifier = Modifier.width(5.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Single Hourly Item: loads the data and packs them
// -----------------------------------------------------------------------------
@Composable
fun HourlyItem(forecast : Forecast?, hour : Int) {
    val HourlyData = forecast?.getHourlyData(hour)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp).wrapContentHeight()
    ) {
        //  Time in Hour
        Text(
            text = HourlyData?.hour?.toString() + " Uhr",
            style = MaterialTheme.typography.medium
        )
        Spacer(modifier = Modifier.height(15.dp))

        //  Icon for the hour
        Image(
            painter = painterResource(id = getIconForWmoCode(HourlyData?.state ?: 0, HourlyData?.isNight ?: false)),
            contentDescription = "",
            modifier = Modifier.size(27.dp)
        )
        Spacer(modifier = Modifier.height(15.dp))

        // Temperature
        Text(
            text = "${HourlyData?.temp}°",
            style = MaterialTheme.typography.temp
        )
    }
}

// -----------------------------------------------------------------------------
// Weekly Block of 7 Daily-Items in a darkened box with a heading
// -----------------------------------------------------------------------------
@Composable
fun SevenDayForecastBlock(forecast : Forecast?) {

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {//Space left/right of box

        // Heading of weekly block
        Text(
            text = "7-Tage-Vorhersage",
            style = MaterialTheme.typography.mediumHeading
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Overlay darkened box with max width and rounded corners
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().clip(RoundedCornerShape(12.dp))) {
            Box(
                modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f))
            )
            //Column with all rows of Daily Items
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)//Space between Daily Rows
            ) {
                (0 until 7).forEach { day -> DailyItem(forecast, day)
                }
            }
        }
    }

}

// -----------------------------------------------------------------------------
// Single Daily-Item: loads the data and packs them
// -----------------------------------------------------------------------------
@Composable
fun DailyItem(forecast: Forecast?, day : Int) {
    val DailyData = forecast?.getDailyData(day)//get forecast daily data

    // All content in a row
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        // 1) Day-Label on DailyItem
        Text(
            text = DailyData?.dayLabel ?: "Fehler",
            style = MaterialTheme.typography.medium,
            modifier = Modifier.weight(1.2f).testTag("day")
        )
        //Spacer(modifier = Modifier.width(10.dp))//Spacer between Day-label and rain prob
        // 2) Rain probability and drop icon in a row
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,) {
            // The icon
            Image(
                painter = painterResource(id = R.drawable.dropp),
                contentDescription = "",
                modifier = Modifier.size(9.dp).testTag("Image")
            )
            Spacer(modifier = Modifier.width(3.dp))
            // The rain probability
            Text(
                text = " " + DailyData?.rain.toString() + "%",
                style = MaterialTheme.typography.medium,
            )
        }
        //Spacer(modifier = Modifier.width(25.dp))//Spacer between rain prob and image

        // 3) Weather-Icon in a box
        Box(modifier = Modifier.weight(1.3f), contentAlignment = Alignment.Center) {
            val (wmoCode, isNight) = forecast!!.getWmoCodeAndIsNight()
            val iconRes = getIconForWmoCode(wmoCode, isNight)
            Image(
                painter = painterResource(id = getIconForWmoCode(DailyData?.state ?: 0, false)),
                contentDescription = "",
                modifier = Modifier.size(27.dp).testTag("Image")
            )
        }
        //Spacer(modifier = Modifier.width(25.dp))//Spacer between icon and high temperature

        // 4) Arrow up image + highest temperature in a row
        Row(modifier = Modifier.weight(0.8f), verticalAlignment = Alignment.CenterVertically,) {
            Image(
                painter = painterResource(id = R.drawable.up),
                contentDescription = "",
                modifier = Modifier.size(22.dp).weight(1f).testTag("Image")
            )
            Spacer(modifier = Modifier.width(1.dp))
            Text(
                text = DailyData?.max.toString() + "°",
                style = MaterialTheme.typography.temp,
            )
        }

        // 5) Arrow down image + lowest temperature in a row
        Row(modifier = Modifier.weight(0.8f), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.down),
                contentDescription = "",
                modifier = Modifier.size(22.dp).testTag("Image")
            )
            Spacer(modifier = Modifier.width(1.dp))
            Text(
                text = DailyData?.min.toString() + "°",
                style = MaterialTheme.typography.temp

            )
        }
    }
}

// -----------------------------------------------------------------------------
// Info-boxes: humidity, rain probability today & tomorrow, sunrise, sunset
// -----------------------------------------------------------------------------
@Composable
fun InfoBoxesSection(forecast: Forecast?) {
    //Spacer(modifier = Modifier.height(16.dp))

    //First row of the Boxes
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

        // Box 1: Humidity
        Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(12.dp))) {
            // Box for background
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f)))
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(//Heading
                    text = "Luftfeuchtigkeit",
                    style = MaterialTheme.typography.boxHeading
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(//Data
                    text = forecast?.days?.firstOrNull()?.hourlyValues?.firstOrNull()?.relativeHumidity.toString() + " %",
                    style = MaterialTheme.typography.boxText
                )
            }
        }

        // Box 2: Rain probability
        Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(12.dp))) {
            // Box for background
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f)))
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(//Heading
                    text = "Niederschlag heute",
                    style = MaterialTheme.typography.boxHeading
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(//Data today
                    text = forecast?.days?.firstOrNull()?.hourlyValues?.maxOfOrNull { it.rain }?.roundToInt().toString() + " mm",
                    style = MaterialTheme.typography.boxText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(//Data tomorrow
                    text = forecast?.days?.getOrNull(1)?.hourlyValues?.maxOfOrNull { it.rain }?.roundToInt().toString() + " mm morgen erwartet",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    //Second row of Boxes
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

        // Box 3: Sunrise
        Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(12.dp))) {
            // Box for background
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f)))

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(//Heading
                    text = "Sonnenaufgang",
                    style = MaterialTheme.typography.boxHeading
                )
                Spacer(modifier = Modifier.height(7.dp))
                Text(//Data
                    text = forecast?.days?.firstOrNull()?.sunrise?.time.toString(),
                    style = MaterialTheme.typography.boxText
                )
            }
        }

        // Box 4: Sunset
        Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(12.dp)),) {
            // Box for background
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f)))
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(//heading
                    text = "Sonnenuntergang",
                    style = MaterialTheme.typography.boxHeading
                    )
                Spacer(modifier = Modifier.height(7.dp))
                Text(//Data
                    text = forecast?.days?.firstOrNull()?.sunset?.time.toString(),
                    style = MaterialTheme.typography.boxText
                )
            }
        }
    }
}
