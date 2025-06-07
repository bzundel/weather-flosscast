package de.frauas.weather_flosscast.ui

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
import androidx.compose.ui.res.painterResource
import de.frauas.weather_flosscast.R
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.IconButton


/**
 * Einfaches Datenmodell für die Dummy-Liste von Städten.
 */
data class CityWeather(
    val cityName: String,
    val currentTemp: Int,
    val condition: String,
    val highTemp: Int,
    val lowTemp: Int,
    val lastUpdated: String
)

//Dummy-data: drei Beispiel-Städte
private val dummyCities = listOf(
    CityWeather("Frankfurt am Main", 20, "Regen",       24, 16, "16:15"),
    CityWeather("Berlin",    23, "Sonnig",      25, 18, "16:15"),
    CityWeather("München",    9, "Schneeschauer", 11,  4, "16:15")
)

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
    onSearch: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1) searchfield with icon-button to search
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Stadt oder Flughafen suchen", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),

            trailingIcon = {
                IconButton(onClick = {
                    // Wenn der Nutzer auf das Lupen-Icon klickt
                    //onSearch(query)
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

            //2) List
            val filteredCities = dummyCities.filter { it.cityName.contains(query, ignoreCase = true) }
            Column {
                filteredCities.forEach { city ->
                    CityCard(city) {
                        //onCitySelected(city.cityName)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

//Cards for the citys
@Composable
fun CityCard(city: CityWeather, onClick: () -> Unit) {
    // Hintergrundfarbe je nach Wetterbedingung
    val bgColor = colorForCondition(city.condition)

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
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            //Icon of current condition
            val iconRes = when (city.condition.lowercase()) {
                "sonnig", "klar", "heiter"       -> R.drawable.sun
                "regen", "regnerisch", "niesel"  -> R.drawable.rain
                "schnee", "schneeschauer"        -> R.drawable.snow
                else                              -> null
            }

            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = city.condition,
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
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    //last updates Uhrzeit
                    Text(
                        text = city.lastUpdated,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White),

                    )
                //city current weather condition
                Text(
                    text = city.condition,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    fontSize = 14.sp
                )
            }

            //temperatures in a column
            Column (){
                //current temperatur
                Text(
                    text = "${city.currentTemp}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    color = Color.White
                )
                //Space
                Spacer(modifier = Modifier.width(18.dp))

                //High and low temperatures under
                Text(
                    text = "${city.highTemp}°/${city.lowTemp}°",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                )
            }

        }
    }
}






@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(onCitySelected = {},
        onSearch = { query ->
            // Beispiel-Callback:
            // Hier könntest du navigieren, Toast anzeigen, Liste neu laden...
            println("Suchtext abgeschickt: $query")
        })
}
