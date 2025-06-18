package de.frauas.weather_flosscast.ui

import org.junit.Before
import org.junit.Test
import kotlin.random.Random
import org.junit.Assert.*

//Tests if the right weather code description gets returned on weather specified weather code
class UtilsWmoCodeHandlingTest {
    var mockWmoCode: Int = 0

    //Creates random weather code
    @Before
    fun randomMockWmoCode(){
        mockWmoCode = Random.nextInt(0, 101)
    }

    //Tests if the right description gets returned with random weather code
    @Test
    fun getConditionForRandomWmoCodeTest(){
        val expectedString =  when (mockWmoCode) {
            0  -> "Klar"
            1  -> "Hauptsächlich klar"
            2  -> "Teilweise bewölkt"
            3  -> "Bedeckt"
            in 45..48    -> "Nebel"
            in 51..55    -> "Nieselregen"
            in 56..57    -> "Gefrierender Nieselregen"
            in 61..65    -> "Regen"
            in 66..67    -> "Gefrierender Regen"
            in 71..75    -> "Schneefall"
            77           -> "Schneekörner"
            in 80..82    -> "Regenschauer"
            in 85..86    -> "Schneeschauer"
            in 95..99    -> "Gewitter"
            else         -> "Unbekannt"
        }
        assertEquals(getConditionForWmoCode(mockWmoCode), expectedString)
    }

    @Test
    fun getConditionForWmoCode0Test(){
        mockWmoCode = 0
        assertEquals(getConditionForWmoCode(mockWmoCode), "Klar")
    }

    @Test
    fun getConditionForWmoCode1Test(){
        mockWmoCode = 1
        assertEquals(getConditionForWmoCode(mockWmoCode), "Hauptsächlich klar")
    }

    @Test
    fun getConditionForWmoCode2Test(){
        mockWmoCode = 2
        assertEquals(getConditionForWmoCode(mockWmoCode), "Teilweise bewölkt")
    }

    @Test
    fun getConditionForWmoCode3Test(){
        mockWmoCode = 3
        assertEquals(getConditionForWmoCode(mockWmoCode), "Bedeckt")
    }

    @Test
    fun getConditionForWmoCode45To48Test(){
        mockWmoCode = Random.nextInt(45, 49)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Nebel")
    }

    @Test
    fun getConditionForWmoCode51To55Test(){
        mockWmoCode = Random.nextInt(51, 56)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Nieselregen")
    }

    @Test
    fun getConditionForWmoCode56To57Test(){
        mockWmoCode = Random.nextInt(56, 58)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Gefrierender Nieselregen")
    }

    @Test
    fun getConditionForWmoCode61To65Test(){
        mockWmoCode = Random.nextInt(61, 66)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Regen")
    }

    @Test
    fun getConditionForWmoCode66To67Test(){
        mockWmoCode = Random.nextInt(66, 68)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Gefrierender Regen")
    }

    @Test
    fun getConditionForWmoCode71To75Test(){
        mockWmoCode = Random.nextInt(71, 76)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Schneefall")
    }

    @Test
    fun getConditionForWmoCode77Test(){
        mockWmoCode = 77
        assertEquals(getConditionForWmoCode(mockWmoCode), "Schneekörner")
    }

    @Test
    fun getConditionForWmoCode80To83Test(){
        mockWmoCode = Random.nextInt(80, 83)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Regenschauer")
    }

    @Test
    fun getConditionForWmoCode85To86Test(){
        mockWmoCode = Random.nextInt(85, 87)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Schneeschauer")
    }

    @Test
    fun getConditionForWmoCode95To99Test(){
        mockWmoCode = Random.nextInt(95, 100)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Gewitter")
    }

    @Test
    fun getConditionForUnknownWmoCodeTest(){
        mockWmoCode = Random.nextInt(100, 1000)
        assertEquals(getConditionForWmoCode(mockWmoCode), "Unbekannt")
    }
}