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
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.frauas.weather_flosscast.R
import com.airbnb.lottie.compose.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.CityList
import de.frauas.weather_flosscast.Forecast
import de.frauas.weather_flosscast.getCitySearchResults
import de.frauas.weather_flosscast.getForecastFromCacheOrDownload
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import java.time.LocalDateTime

// -----------------------------------------------------------------------------
// Screen
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(cityName : String, onBack: () -> Unit) {
    val context = LocalContext.current  // Safe save for app context for Compose

    val city = CityList.getCities(context).find { it.cityName == cityName } //Getting cityData from CityList with find function
    var forecast by remember { mutableStateOf<Forecast?>(null) }            //Stores the forecast state and recomposition when updated


    var isRefreshing by remember { mutableStateOf(false) }// Pull-to-Refresh state
    val scope      = rememberCoroutineScope()
    val swipeState = rememberSwipeRefreshState(isRefreshing)

    //Getting forecast data for city that was handed over to WeatherScreen/////////////////////////////////////////
    LaunchedEffect(cityName) {
        if (city != null) {
            forecast =
                getForecastFromCacheOrDownload(context.filesDir, city!!.latitude, city!!.longitude)

        }
        if (city == null) {
            Toast.makeText(context, "Fehler, die Stadt exisitert nicht!", Toast.LENGTH_SHORT).show()  //if the city variable is empty -> info and go back to searchScreen
            onBack()
        }
    }

        val currentForecast: Forecast = forecast ?: return  //currentForecast can not be empty, if empty return
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    val lifecycleOwner = LocalLifecycleOwner.current    //Debugging
    val filesDir = LocalContext.current.filesDir        //Debugging



    // 2)
    val (wmoCode, isNight) = forecast!!.getWmoCodeAndIsNight()

    // 3) Hintergrundfarbe
    val bgC = colorForWmoCode(wmoCode, isNight)

// SwipeRefresh component over the rest of the components
    // It refreshed the forecast-data when swiping down with indicator
    SwipeRefresh(
        state     = swipeState,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                forecast = loadForecastsForCities(context, CityList.getCities(context))[cityName]
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(bgC),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1) Header with Lottie-animation
        item {
            Spacer(modifier = Modifier.height(16.dp))
            WeatherHeader(cityName, currentForecast, onBack)
        }

        // 2) hourly forecast
        item {
            HourlyForecastRow(forecast)
        }

        // 3) weekly forecast
         item {
            SevenDayForecastBlock(forecast)
        }

        // 4) the 4 infoboxes
        item {
            InfoBoxesSection(forecast)
        }

        // button for debugging
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    lifecycleOwner.lifecycleScope.launch {
                        val forecast: Forecast = getForecastFromCacheOrDownload(filesDir, 50.1, 8.6)
                        val matches: List<City> = getCitySearchResults("Frankfurt")

                        val dummy = Unit
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text("Get forecast")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }}
}

// -----------------------------------------------------------------------------
//Header
// -----------------------------------------------------------------------------
@Composable
 fun WeatherHeader(cityName : String, forecast: Forecast, onBack:   () -> Unit) {

    //  Select the right lottie-animation with data from getWMOCode function
    val(wmoCode, isNight) = forecast.getWmoCodeAndIsNight()
    val lottieRes = getLottieResForWmoCode(wmoCode, isNight)



    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {

        Row(modifier = Modifier.fillMaxSize().padding(start = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {


            //Lottie-animation
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(100.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()  ) {

                Text(//Stadtname
                    text = cityName,
                    color = Color.White,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .clickable { onBack()}  // ruft SearchScreen auf
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(// Temperatur
                    text = "${forecast.getCurrentTemperature()}" + "°",
                    color = Color.White,
                    fontSize = 60.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))

                val code      = forecast?.days?.firstOrNull()?.hourlyValues?.firstOrNull()?.weatherCode ?: 0
                val condition = getConditionForWmoCode(code)
                Text(// Zustand und High/Low
                    text = "${condition} ",// " + "${getDailyMaxTemp(forecast)}° / " + "${getDailyMinTemp(forecast)}°
                    color = Color.White,
                    fontSize = 22.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
//hourly
// -----------------------------------------------------------------------------
@Composable
 fun HourlyForecastRow(forecast : Forecast?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        //Text(
        //    text = "Stunden-Vorhersage",
        //    style = MaterialTheme.typography.titleMedium.copy(color = Color.Black)
        //
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                // 1. Wir schneiden die Box zuerst auf abgerundete Ecken zu:
                .clip(RoundedCornerShape(12.dp))


        ) {
            // 3. Halbtransparente, dunkle Überlagerung:
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.15f))
            )
            // 4. Der Inhalt (LazyRow) wird über dem Overlay gezeichnet:
            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                //contentPadding = PaddingValues(horizontal = 2.dp),
                //horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items((0 until 24).toList()) { hour ->  //Erstelle 24 HourlyItems 0-24Std
                    HourlyItem(forecast, hour = hour)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
//single element in hourly
// -----------------------------------------------------------------------------
@Composable
fun HourlyItem(forecast : Forecast?, hour : Int) {
    // Jede Stunde nur als Text, ohne extra Card-Hintergrund
    val HourlyData = forecast?.getHourlyData(hour)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .wrapContentHeight()
    ) {
        Text(
            text = HourlyData?.hour?.toString() + " Uhr",//hourly.hourLabel,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(17.dp))


        val (wmoCode, isNight) = forecast!!.getWmoCodeAndIsNight()
        val iconRes = getIconForWmoCode(wmoCode, isNight)

        // ADD IMAGE
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "",
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(17.dp))
        Text(
            text = "${HourlyData?.temp}°",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )
    }
}

// -----------------------------------------------------------------------------
//weekly
// -----------------------------------------------------------------------------
@Composable
fun SevenDayForecastBlock(forecast : Forecast?) {
    Spacer(modifier = Modifier.height(10.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "7-Tage-Vorhersage",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.15f))
            )

            // Hier eine einfache Column statt LazyColumn:
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                (0 until 7).forEach { day ->  // 7 Tage: Heute + 6
                    DailyItem(forecast, day)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
//single day in daily overview for the 7-Days Overview
// -----------------------------------------------------------------------------
@Composable
fun DailyItem(forecast: Forecast?, day : Int) {
    //Getting needed data with a function
    val DailyData = forecast?.getDailyData(day)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = DailyData?.dayLabel ?: "Fehler",          //Day-Label on DailyItem
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.weight(1f)
        )

        /////////////////////////rain probability icon//////////////////////
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = painterResource(id = R.drawable.dropp),//weatherIconResForCode(weatherCode)
            contentDescription = "",
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = " " + DailyData?.rain.toString() + " %",     //Rain probability
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(30.dp))

        val (wmoCode, isNight) = forecast!!.getWmoCodeAndIsNight()
        val iconRes = getIconForWmoCode(wmoCode, isNight)

        Image(
            painter = painterResource(id = iconRes),//weatherIconResForCode(weatherCode) ICON
            contentDescription = "",
            modifier = Modifier.size(25.dp)
        )
        Spacer(modifier = Modifier.width(40.dp))

        Image(
            painter = painterResource(id = R.drawable.up),//weatherIconResForCode(weatherCode)  ICON
            contentDescription = "",
            modifier = Modifier.size(25.dp)
        )

        Text(
            text = DailyData?.max.toString() + "°",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )

        Image(
            painter = painterResource(id = R.drawable.down),//weatherIconResForCode(weatherCode)
            contentDescription = "",
            modifier = Modifier.size(25.dp)
        )

        //Spacer(modifier = Modifier.width(29.dp))
        Text(
            text = DailyData?.min.toString() + "°",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )
    }
}

// -----------------------------------------------------------------------------
//info-boxes
// -----------------------------------------------------------------------------
    @Composable
    fun InfoBoxesSection(forecast: Forecast?) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Box 1: UV-INDEX
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Die halbtransparente, dunkle Überlagerung
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
                // Dein Content darüber
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    //verticalArrangement = Arrangement.SpaceBetween
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Luftfeuchtigkeit",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                    )
                    Text(
                        text = forecast?.days?.firstOrNull()?.hourlyValues?.firstOrNull()?.relativeHumidity.toString() + " %",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
                    )
                }
            }

            // Box 2: Niederschlag
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Halbtransparente, dunkle Überlagerung
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Niederschlag heute",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                    )
                    Text(
                        text = forecast?.days?.firstOrNull()?.hourlyValues?.maxOfOrNull { it.rain }?.roundToInt().toString() + " mm",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
                    )
                    Text(
                        text = forecast?.days?.getOrNull(1)?.hourlyValues?.maxOfOrNull { it.rain }?.roundToInt().toString() + " mm morgen erwartet",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Box 3: Wind
           /* Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                // Die halbtransparente, dunkle Überlagerung
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
                // Dein Content darüber
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Text(
                        text = "Wind",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                    )
                    Text(
                        text = "6 km/h",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
                    )
                    Text(
                        text = "Richtung SW",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
            }*/ //Can be used later

            // Box 4:
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),

            ) {
                // Halbtransparente, dunkle Überlagerung
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                    //verticalArrangement = Arrangement.SpaceBetween

                ) {
                    Text(
                        text = "Regen-%",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White),

                    )
                    Text(
                        text = "5%",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
                    )
                    Text(
                        text = "morgen 7%",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
            }
        }
    }




// -----------------------------------------------------------------------------
//preview in android studio
// -----------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherScreen("Offenbach am Main", onBack = {})
}