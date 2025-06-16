package de.frauas.weather_flosscast.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.frauas.weather_flosscast.R
import com.airbnb.lottie.compose.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import de.frauas.weather_flosscast.CityList
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.getForecastFromCacheOrDownload
import kotlin.math.roundToInt
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(cityName : String, onBack: () -> Unit) {
    val context = LocalContext.current                                      // Safe save for app context for Compose
    val city = CityList.getCities(context).find { it.cityName == cityName } //Getting cityData from CityList with find function
    var forecast by remember { mutableStateOf<Forecast?>(null) }            //Stores the forecast state and recomposition when updated


    var isRefreshing by remember { mutableStateOf(false) }                  // Pull-to-Refresh state
    val scope      = rememberCoroutineScope()
    val swipeState = rememberSwipeRefreshState(isRefreshing)

    //Getting forecast data @launch//
    LaunchedEffect(cityName) {
        if (city != null) { forecast = getForecastFromCacheOrDownload(context.filesDir, city!!.latitude, city!!.longitude) }
        if (city == null) {
            Toast.makeText(context, "Fehler, die Stadt exisitert nicht!", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }
    val currentForecast: Forecast = forecast ?: return          // load current forecast in variable
    val (wmoCode, isNight) = forecast!!.getWmoCodeAndIsNight()  //gets wmo-code and boolean for night-check
    val bgC = colorForWmoCode(wmoCode, isNight)                 //sets the background color for the whole screen

    // SwipeRefresh component over the rest of the components, It refreshed the forecast-data when swiping down with indicator
    SwipeRefresh(
        state = swipeState,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                forecast = city?.let {
                    getForecastFromCacheOrDownload(context.filesDir, it.latitude, it.longitude, true)
                } ?: throw IllegalArgumentException("City must not be null")    //getting new Forecast with null protection
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
    ) {
        // Column of whole content
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(bgC),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {// 1) Header with Lottie-animation
                Spacer(modifier = Modifier.height(16.dp))
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
                    color = Color.White,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .clickable { onBack()}
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Temperature
                Text(
                    text = "${forecast.getCurrentTemperature()}" + "°",
                    color = Color.White,
                    fontSize = 60.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Condition
                Text(
                    text = condition,
                    color = Color.White,
                    fontSize = 22.sp
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
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Overlay darkened box with max width and rounded corners
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp))) {
            Box(
                modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f))
            )
            LazyRow(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),) {
                items((0 until 24).toList()) { hour ->  //creates 24 hourly items
                    HourlyItem(forecast, hour = hour)
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
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(17.dp))

        //  Icon for the hour
        Image(
            painter = painterResource(id = getIconForWmoCode(HourlyData?.state ?: 0, HourlyData?.isNight ?: false)),
            contentDescription = "",
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(17.dp))

        // Temperature
        Text(
            text = "${HourlyData?.temp}°",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )
    }
}

// -----------------------------------------------------------------------------
// Weekly Block of 7 Daily-Items in a darkened box with a heading
// -----------------------------------------------------------------------------
@Composable
fun SevenDayForecastBlock(forecast : Forecast?) {
    Spacer(modifier = Modifier.height(10.dp))
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        // Heading of weekly block
        Text(
            text = "7-Tage-Vorhersage",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Overlay darkened box with max width and rounded corners
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().clip(RoundedCornerShape(12.dp))) {
            Box(
                modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.15f))
            )
            //Column with all rows of Daily Items
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.weight(1.5f)
        )
        Spacer(modifier = Modifier.width(10.dp))//Spacer between Day-label and rain prob
        // 2) Rain probability and drop icon in a row
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,) {
            // The icon
            Image(
                painter = painterResource(id = R.drawable.dropp),
                contentDescription = "",
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            // The rain probability
            Text(
                text = " " + DailyData?.rain.toString() + " %",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.width(25.dp))//Spacer between rain prob and image

        // 3) Weather-Icon in a box
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val (wmoCode, isNight) = forecast!!.getWmoCodeAndIsNight()
            val iconRes = getIconForWmoCode(wmoCode, isNight)
            Image(
                painter = painterResource(id = getIconForWmoCode(DailyData?.state ?: 0, false)),//weatherIconResForCode(weatherCode) ICON
                contentDescription = "",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.width(25.dp))//Spacer between icon and high temperature

        // 4) Arrow up image + highest temperature in a row
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically,) {
            Image(
                painter = painterResource(id = R.drawable.up),//weatherIconResForCode(weatherCode)  ICON
                contentDescription = "",
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = DailyData?.max.toString() + "°",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
        }

        // 5) Arrow down image + lowest temperature in a row
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.down),//weatherIconResForCode(weatherCode)
                contentDescription = "",
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = DailyData?.min.toString() + "°",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Info-boxes: humidity, rain probability today & tomorrow, sunrise, sunset
// -----------------------------------------------------------------------------
@Composable
fun InfoBoxesSection(forecast: Forecast?) {
    Spacer(modifier = Modifier.height(16.dp))

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
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(//Data
                    text = forecast?.days?.firstOrNull()?.hourlyValues?.firstOrNull()?.relativeHumidity.toString() + " %",
                    style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
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
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(//Data today
                    text = forecast?.days?.firstOrNull()?.hourlyValues?.maxOfOrNull { it.rain }?.roundToInt().toString() + " mm",
                    style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
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
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(//Data
                    text = forecast?.days?.firstOrNull()?.sunrise?.time.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
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
                    style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
                    )
                Spacer(modifier = Modifier.height(3.dp))
                Text(//Data
                    text = forecast?.days?.firstOrNull()?.sunset?.time.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
                )
            }
        }
    }
}