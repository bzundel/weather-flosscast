package de.frauas.weather_flosscast.ui

import de.frauas.weather_flosscast.R
import org.junit.Test
import org.junit.Assert.*
import kotlin.random.Random

//Tests to check if the right Lottie Icons get returned for the specified weather code
class UtilsLottieResTest {
    //Tests icon for random weather code at night
    @Test
    fun getIconForRandomNightWmoCode(){
        val randomWmoCode = Random.nextInt(0, 101)
        val expectedLottieRes = when (randomWmoCode) {
            0                                  -> R.raw.mond              // klarer Himmel
            in 1..3                      -> R.raw.mondundwolken     // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.raw.gewitter          // Gewitter/Trichterwolke
            in 23..24, 26                -> R.raw.mondschnee        // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.raw.mondschnee        // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.raw.mondregen         // Drizzle / Rain / Showers
            else                               -> R.raw.mondundwolken     // Nebel, Staub, sonstige Wolken
        }

        assertEquals(getLottieResForWmoCode(randomWmoCode, true), expectedLottieRes)
    }

    //Tests icon for random weather code at day
    @Test
    fun getIconForRandomDayWmoCode(){
        val randomWmoCode = Random.nextInt(0, 101)
        val expectedLottieRes = when (randomWmoCode) {
            0                                  -> R.raw.sonne             // klarer Himmel
            in 1..3                      -> R.raw.sonnewolken       // Wolkenauf-/-abbau
            13, 17, 19, in 90..99        -> R.raw.gewitter          // Gewitter/Trichterwolke
            in 23..24, 26                -> R.raw.schnee            // Schneeregen / gefrierender Niederschlag
            22, in 70..79                -> R.raw.schnee            // Schnee / Schneeschauer
            in 20..21, 25,
            in 50..59, in 60..69,
            in 80..89                    -> R.raw.rain              // Drizzle / Rain / Showers
            else                               -> R.raw.wolken            // Nebel, Staub, sonstige Wolken
        }

        assertEquals(getLottieResForWmoCode(randomWmoCode, false), expectedLottieRes)
    }

    @Test
    fun lottieResNightMondTest(){
        assertEquals(getLottieResForWmoCode(0, true), R.raw.mond)
    }

    @Test
    fun lottieResNightMondUndWolkenTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(1,4), true), R.raw.mondundwolken)
    }

    @Test
    fun lottieResNightGewitterTest(){
        assertEquals(getLottieResForWmoCode(13, true), R.raw.gewitter)
        assertEquals(getLottieResForWmoCode(17, true), R.raw.gewitter)
        assertEquals(getLottieResForWmoCode(19, true), R.raw.gewitter)
        assertEquals(getLottieResForWmoCode(Random.nextInt(90,100), true), R.raw.gewitter)
    }

    @Test
    fun lottieResNightMondschneeTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(23,25), true), R.raw.mondschnee)
        assertEquals(getLottieResForWmoCode(26, true), R.raw.mondschnee)
    }

    @Test
    fun lottieResNightSchneeTest(){
        assertEquals(getLottieResForWmoCode(22, true), R.raw.mondschnee)
        assertEquals(getLottieResForWmoCode(Random.nextInt(70,80), true), R.raw.mondschnee)
    }

    @Test
    fun lottieResNightMondregenTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(20,22), true), R.raw.mondregen)
        assertEquals(getLottieResForWmoCode(25, true), R.raw.mondregen)
        assertEquals(getLottieResForWmoCode(Random.nextInt(50,60), true), R.raw.mondregen)
        assertEquals(getLottieResForWmoCode(Random.nextInt(60,70), true), R.raw.mondregen)
        assertEquals(getLottieResForWmoCode(Random.nextInt(80,90), true), R.raw.mondregen)
    }

    @Test
    fun lottieResNightUnknownTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(4,13), true), R.raw.mondundwolken)
        assertEquals(getLottieResForWmoCode(Random.nextInt(14,17), true), R.raw.mondundwolken)
        assertEquals(getLottieResForWmoCode(18, true), R.raw.mondundwolken)
        assertEquals(getLottieResForWmoCode(Random.nextInt(90,100), true), R.raw.mondundwolken)
    }

    @Test
    fun lottieResDaySonneTest(){
        assertEquals(getLottieResForWmoCode(0, false), R.raw.sonne)
    }

    @Test
    fun lottieResDaySonneWolkenTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(1,4), false), R.raw.sonnewolken)
    }

    @Test
    fun lottieResDayGewitterTest(){
        assertEquals(getLottieResForWmoCode(13, false), R.raw.gewitter)
        assertEquals(getLottieResForWmoCode(17, false), R.raw.gewitter)
        assertEquals(getLottieResForWmoCode(19, false), R.raw.gewitter)
        assertEquals(getLottieResForWmoCode(Random.nextInt(90,100), false), R.raw.gewitter)
    }

    @Test
    fun lottieResDaySchneeRegenTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(23,25), false), R.raw.schnee)
        assertEquals(getLottieResForWmoCode(26, false), R.raw.schnee)
    }

    @Test
    fun lottieResDaySchneeTest(){
        assertEquals(getLottieResForWmoCode(22, false), R.raw.schnee)
        assertEquals(getLottieResForWmoCode(Random.nextInt(70,80), false), R.raw.schnee)
    }

    @Test
    fun lottieResDayRainTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(20,22), false), R.raw.rain)
        assertEquals(getLottieResForWmoCode(25, false), R.raw.rain)
        assertEquals(getLottieResForWmoCode(Random.nextInt(50,60), false), R.raw.rain)
        assertEquals(getLottieResForWmoCode(Random.nextInt(60,70), false), R.raw.rain)
        assertEquals(getLottieResForWmoCode(Random.nextInt(80,90), false), R.raw.rain)
    }

    @Test
    fun lottieResDayUnknownTest(){
        assertEquals(getLottieResForWmoCode(Random.nextInt(4,13), false), R.raw.wolken)
        assertEquals(getLottieResForWmoCode(Random.nextInt(14,17), false), R.raw.wolken)
        assertEquals(getLottieResForWmoCode(18, false), R.raw.wolken)
        assertEquals(getLottieResForWmoCode(Random.nextInt(90,100), false), R.raw.wolken)
    }
}