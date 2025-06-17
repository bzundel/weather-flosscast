package de.frauas.weather_flosscast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable



@Composable
fun WeatherflosscastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = Typography,
        content = content
    )
}
