package de.frauas.weather_flosscast.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

//Probedaten//

data class HourlyEntry(val hourLabel: String, val temp: Int)
private val dummyHourly: List<HourlyEntry> = run {
    val list = mutableListOf<HourlyEntry>()
    // Erster Eintrag = "Now"
    list += HourlyEntry("Now", (10..30).random())
    // Einträge 1 Uhr bis 23 Uhr
    for (hour in 1..23) {
        val temp = (10..30).random()
        list += HourlyEntry("$hour Uhr", temp)
    }
    list.toList()
}
data class DailyEntry(val dayLabel: String, val high: Int, val low: Int)
private val dummyWeekly = listOf(
    DailyEntry("Heute", 25, 18),
    DailyEntry("Di",    23, 16),
    DailyEntry("Mi",    24, 17),
    DailyEntry("Do",    24, 18),
    DailyEntry("Fr",    28, 20),
    DailyEntry("Sa",    29, 22),
    DailyEntry("So",    30, 21)
)

// -----------------------------------------------------------------------------
// function for select the background-colour
// -----------------------------------------------------------------------------

private fun colorForBackground(condition: String): Color {
    return when (condition.lowercase()) {
        "regen", "regnerisch", "niesel" -> Color(0xFF808080)   // Dunkelgrau
        "sonnig", "klar", "heiter"        -> Color(0xFF33AAFF)   // Blau
        "schnee", "schneeschauer"         -> Color(0xFFB0BEC5)   // Hellgrau
        else                               -> Color(0xFF546E7A)   //
    }
}

//einstellen hintergrund und name
val bgC = colorForBackground("sonnig")
val cityName = "Offenbach"

// -----------------------------------------------------------------------------
// Screen complete
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(cityName: String, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(bgC),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1) Header with Lottie-animation
        item {
            Spacer(modifier = Modifier.height(16.dp))
            WeatherHeader()
        }

        // 2) hourly forecast
        item {
            HourlyForecastRow()
        }

        // 3) weekly forecast
         item {
            SevenDayForecastBlock()
        }

        // 4) the 4 infoboxes
        item {
            InfoBoxesSection()
        }
    }
}

// -----------------------------------------------------------------------------
//Header
// -----------------------------------------------------------------------------
@Composable
 fun WeatherHeader() {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(bgC)) {

        Row(modifier = Modifier.fillMaxSize().padding(start = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {

            /*Image(
                    painter = painterResource(id=R.drawable.sun_svgrepo_com),
                    contentDescription = "",
                    modifier = Modifier
                        .size(80.dp)
                        //.padding(end = 32.dp)
                )*/
            //Lottie-animation
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gewitter))
            val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
            LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(90.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()  ) {

                Text(//Stadtname
                    text = cityName,
                    color = Color.White,
                    fontSize = 24.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(// Temperatur
                    text = "23°", // Hier kannst du den echten Wert einsetzen
                    color = Color.White,
                    fontSize = 64.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(// Zustand und High/Low
                    text = "Sonnig  25°/16°",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
//hourly
// -----------------------------------------------------------------------------
@Composable
 fun HourlyForecastRow() {
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
                items(dummyHourly) { hourly ->
                    HourlyItem(hourly)
                }
            }
        }
    }

}

// -----------------------------------------------------------------------------
//weekly
// -----------------------------------------------------------------------------
@Composable
fun SevenDayForecastBlock() {
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
                dummyWeekly.forEach { daily ->
                    DailyItem(daily)
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
//info-boxes
// -----------------------------------------------------------------------------
    @Composable
    fun InfoBoxesSection() {
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
                        text = "UV-INDEX",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White)
                    )
                    Text(
                        text = "5",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
                    )
                    Text(
                        text = "Den restlicher Tag Mittel",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
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
                        text = "0 mm",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
                    )
                    Text(
                        text = "0 mm morgen erwartet",
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
            }

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
//single element in hourly
// -----------------------------------------------------------------------------
    @Composable
    fun HourlyItem(hourly: HourlyEntry) {
        // Jede Stunde nur als Text, ohne extra Card-Hintergrund
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(60.dp)
                .wrapContentHeight()
        ) {
            Text(
                text = hourly.hourLabel,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
            Spacer(modifier = Modifier.height(17.dp))

            // ADD IMAGE
            Image(
                painter = painterResource(id = R.drawable.cloud),
                contentDescription = "",
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(17.dp))
            Text(
                text = "${hourly.temp}°",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
        }
    }

// -----------------------------------------------------------------------------
//single day in daily overview
// -----------------------------------------------------------------------------
    @Composable
    fun DailyItem(daily: DailyEntry) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = daily.dayLabel,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.weight(1f)
            )

            /////////////////////////example rain probability//////////////////////
            //Spacer(modifier = Modifier.width(16.dp))
            /*Image(
                painter = painterResource(id = R.drawable.dropp),//weatherIconResForCode(weatherCode)
                contentDescription = "",
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = " 0%",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.weight(1f)
            )*/
            //Spacer(modifier = Modifier.width(30.dp))

            Image(
                painter = painterResource(id = R.drawable.sun),//weatherIconResForCode(weatherCode)
                contentDescription = "",
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(110.dp))
            Text(
                text = "H:${daily.high}°",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.width(29.dp))
            Text(
                text = "T:${daily.low}°",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )
        }
    }

// -----------------------------------------------------------------------------
//preview in android studio
// -----------------------------------------------------------------------------
    @Preview(showBackground = true)
    @Composable
    fun WeatherScreenPreview() {
        WeatherScreen(cityName = "Offenbach", onBack = {})
    }