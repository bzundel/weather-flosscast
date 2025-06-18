package de.frauas.weather_flosscast

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.io.File


@RunWith(AndroidJUnit4::class)
class ForecastCacheTest {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    val testDirectory: File = context.getDir("Test", 0)

    val latitudeFrankfurt = 50.1
    val longitudeFrankfurt = 8.6

    lateinit var randomForecast: Forecast
    @Before
    fun createMockForecasts(){
        randomForecast = generateMockForecast()
    }
    @Test
    fun noCacheTest() = runTest{
        val file = File(testDirectory, "cache.json")
        if(file.exists()){
            file.delete()
        }
        getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt)
        assertTrue(file.exists())
    }

    @Test
    fun expiredCache() = runTest{
        val expiredForecast = generateExpiredMockForecast()
        val file = File(testDirectory, "cache.json")
        if(file.exists()){
            file.delete()
        }
        file.writeText(jsonifyForecastWithCoordinates(expiredForecast, latitudeFrankfurt, longitudeFrankfurt).toString())
        val newForecast = getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt)
        assertTrue(newForecast.timestamp > expiredForecast.timestamp)
    }

    @Test
    fun forcedCacheUpdate() = runTest {
        val file = File(testDirectory, "cache.json")
        if(file.exists()){
            file.delete()
        }
        file.writeText(jsonifyForecastWithCoordinates(randomForecast, latitudeFrankfurt, longitudeFrankfurt).toString())
        val newForecast = getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt, true)
        assertTrue(newForecast.timestamp > randomForecast.timestamp)
    }

    @Test
    fun newCoordinatesCacheUpdate() = runTest {
        val file = File(testDirectory, "cache.json")
        if(file.exists()){
            file.delete()
        }
        file.writeText(jsonifyForecastWithCoordinates(randomForecast, 10.1, 1.1).toString())
        val newForecast = getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt, true)
        assertTrue(newForecast.timestamp > randomForecast.timestamp)
    }
}