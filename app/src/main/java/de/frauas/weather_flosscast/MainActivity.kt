package de.frauas.weather_flosscast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.frauas.weather_flosscast.ui.theme.WeatherflosscastTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherflosscastTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GreetingWithButton(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        lifecycleScope.launch {
                            val forecast: Forecast =
                                getForecastFromCacheOrDownload(filesDir, 50.1, 8.6)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GreetingWithButton(name: String, modifier: Modifier = Modifier, onButtonClick: () -> Unit) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        Button(onClick = onButtonClick) {
            Text("Fetch data")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherflosscastTheme {
        GreetingWithButton("Android", onButtonClick = { })
    }
}