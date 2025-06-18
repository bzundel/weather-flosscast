package de.frauas.weather_flosscast.ui

import kotlin.random.Random
import androidx.compose.ui.graphics.Color

import org.junit.Assert.*
import org.junit.Test

//Tests if the right background color values get returned
class UtilsBackgroundColorTest {

    //Test if the right color gets returned for random weather codes at nighttime
    @Test
    fun nightColorTest(){
        //Runs function with ramdom Weather code
        val backgroundColor = colorForWmoCode(Random.nextInt(0,101), true)
        //Checks if the right background color is returned
        //Regardless of the weather code the same color should be returned at nighttime
        assertEquals(Color(0xFF37474F), backgroundColor)
    }

    //Test if the right background color gets return with the weather code for a clear sky
    @Test
    fun dayClearSkyColor(){
        //Runs function with clear weather code 0
        val backgroundColor = colorForWmoCode(0, false)
        //Checks if the right background color got returned
        assertEquals(Color(0xFF33AAFF), backgroundColor)
    }

    //Test if the right background color gets returned with snowy or cloudy weather codes
    @Test
    fun snowOrCloudColor(){
        //Runs function with random weather codes for snowy or cloudy weather
        val backgroundColor = colorForWmoCode(Random.nextInt(1,19), false)
        val backgroundColor2 = colorForWmoCode(Random.nextInt(30,50), false)
        val backgroundColor3 = colorForWmoCode(Random.nextInt(70,80), false)
        //Checks the background colors for the different cloudy or snowy weather code ranges
        assertEquals(Color(0xFFB0BEC5), backgroundColor)
        assertEquals(Color(0xFFB0BEC5), backgroundColor2)
        assertEquals(Color(0xFFB0BEC5), backgroundColor3)
    }

    //Test if the right background color gets returned with rainy or stormy weather codes
    @Test
    fun rainOrStormColor(){
        //Runs function with random weather codes for rainy or stormy weather
        val backgroundColor = colorForWmoCode(Random.nextInt(20,30), false)
        val backgroundColor2 = colorForWmoCode(Random.nextInt(50,70), false)
        val backgroundColor3 = colorForWmoCode(Random.nextInt(80,100), false)
        //Checks the background colors for the different rainy or stormy weather code ranges
        assertEquals(Color(0xFF808080), backgroundColor)
        assertEquals(Color(0xFF808080), backgroundColor2)
        assertEquals(Color(0xFF808080), backgroundColor3)
    }

    //Test if the right background color gets returned for unspecified weather codes
    @Test
    fun backupColor(){
        //Runs function with unspecified weather code
        val backgroundColor = colorForWmoCode(Random.nextInt(100,10000), false)
        //Expect fallback backgound color
        assertEquals(Color(0xFFB0BEC5), backgroundColor)
    }
}