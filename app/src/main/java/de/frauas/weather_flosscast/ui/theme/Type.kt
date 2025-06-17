package de.frauas.weather_flosscast.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


// Set of Material typography styles
val Typography = Typography()

val Typography.cityHeader: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp,
        color = Color.White
    )
val Typography.temperatureHeader: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 60.sp,
        color = Color.White
    )
val Typography.conditionHeader: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        color = Color.White
    )
val Typography.temp: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Color.White
    )
val Typography.medium: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = Color.White
    )
val Typography.mediumHeading: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        color = Color.White
    )
val Typography.boxHeading: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = Color.White
    )
val Typography.boxText: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 33.sp,
        color = Color.White
    )
val Typography.cardCity: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 23.sp,
        color = Color.White
    )
val Typography.cardCountry: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 15.sp,
        color = Color.White

    )
val Typography.cardTime: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        color = Color.White
    )
val Typography.cardTemp: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp,
        color = Color.White
    )
val Typography.cardHighLow: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.8f)
    )
val Typography.newcardCity: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 20.sp,
        color = Color.White
    )
val Typography.newCardCountry: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.8f)
    )






