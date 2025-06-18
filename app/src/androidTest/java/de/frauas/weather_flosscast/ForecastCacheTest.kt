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
    //Creates an android context for testing
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    //Creates or opens a Test directory for the testing context
    val testDirectory: File = context.getDir("Test", 0)

    //Mock values used for the tests
    val latitudeFrankfurt = 50.1
    val longitudeFrankfurt = 8.6
    lateinit var randomForecast: Forecast

    //Initializes random forecast mock before every test
    @Before
    fun createMockForecasts(){
        randomForecast = generateMockForecast()
    }

    //Tests if a cache gets created if no cache existed prior
    @Test
    fun noCacheTest() = runTest{
        //Defines Cache file path
        val file = File(testDirectory, "cache.json")
        //Deletes cache if one exists prior
        if(file.exists()){
            file.delete()
        }
        //Runs backend function with mock values
        getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt)
        //Expects that a cache File got created
        assertTrue(file.exists())
    }

    //Tests if a gets updated if an expired Cache exists
    @Test
    fun expiredCache() = runTest{
        //Creates an mock forecast which is expired (timestamp is 3 or more hours old)
        val expiredForecast = generateExpiredMockForecast()
        //Defines cache file path
        val file = File(testDirectory, "cache.json")
        //Deletes cache if one exists prior
        if(file.exists()){
            file.delete()
        }
        //Saves the expired mock forecast as cache file
        file.writeText(jsonifyForecastWithCoordinates(expiredForecast, latitudeFrankfurt, longitudeFrankfurt).toString())
        //Runs function which should detect the expired cache
        val newForecast = getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt)
        //Tests if the cache got updated by comparing the timestamps of the mock expired forecast and the forecast returned by the tested function
        assertTrue(newForecast.timestamp > expiredForecast.timestamp)
    }

    //Tests if the forced cache update flag works
    @Test
    fun forcedCacheUpdate() = runTest {
        //Defines cache file path
        val file = File(testDirectory, "cache.json")
        //Deletes cache if one exists prior
        if(file.exists()){
            file.delete()
        }
        //Saved mock cache file with current timestamp to cache file path
        file.writeText(jsonifyForecastWithCoordinates(randomForecast, latitudeFrankfurt, longitudeFrankfurt).toString())
        //Function gets called with force update flag
        val newForecast = getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt, true)
        //Tests if the cache got updated by comparing the mock timestamp and the returned timestamp by the tested function
        assertTrue(newForecast.timestamp > randomForecast.timestamp)
    }

    //Tests if the cache file gets updated if new coordinates are put in
    @Test
    fun newCoordinatesCacheUpdate() = runTest {
        //Defines cache file path
        val file = File(testDirectory, "cache.json")
        //Deletes cache if one exists prior
        if(file.exists()){
            file.delete()
        }
        //Saves cache file with mock coordinated
        file.writeText(jsonifyForecastWithCoordinates(randomForecast, 10.1, 1.1).toString())
        //Runs function with other mock coordinates
        val newForecast = getForecastFromCacheOrDownload(testDirectory, latitudeFrankfurt, longitudeFrankfurt, true)
        //Tests if a new cache file got created by comparing timestamps
        assertTrue(newForecast.timestamp > randomForecast.timestamp)
    }
}