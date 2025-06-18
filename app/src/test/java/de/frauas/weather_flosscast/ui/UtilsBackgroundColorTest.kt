package de.frauas.weather_flosscast.ui

import kotlin.random.Random
import androidx.compose.ui.graphics.Color

import org.junit.Assert.*
import org.junit.Test

class UtilsBackgroundColorTest {
    @Test
    fun nightColorTest(){
        val backgroundColor = colorForWmoCode(Random.nextInt(0,101), true)
        assertEquals(Color(0xFF37474F), backgroundColor)
    }

    @Test
    fun dayClearSkyColor(){
        val backgroundColor = colorForWmoCode(0, false)
        assertEquals(Color(0xFF33AAFF), backgroundColor)
    }

    @Test
    fun snowOrCloudColor(){
        val backgroundColor = colorForWmoCode(Random.nextInt(1,19), false)
        val backgroundColor2 = colorForWmoCode(Random.nextInt(30,50), false)
        val backgroundColor3 = colorForWmoCode(Random.nextInt(70,80), false)
        assertEquals(Color(0xFFB0BEC5), backgroundColor)
        assertEquals(Color(0xFFB0BEC5), backgroundColor2)
        assertEquals(Color(0xFFB0BEC5), backgroundColor3)
    }

    @Test
    fun rainOrStormColor(){
        val backgroundColor = colorForWmoCode(Random.nextInt(20,30), false)
        val backgroundColor2 = colorForWmoCode(Random.nextInt(50,70), false)
        val backgroundColor3 = colorForWmoCode(Random.nextInt(80,100), false)
        assertEquals(Color(0xFF808080), backgroundColor)
        assertEquals(Color(0xFF808080), backgroundColor2)
        assertEquals(Color(0xFF808080), backgroundColor3)
    }

    @Test
    fun backupColor(){
        val backgroundColor = colorForWmoCode(Random.nextInt(100,10000), false)
        assertEquals(Color(0xFFB0BEC5), backgroundColor)
    }
}