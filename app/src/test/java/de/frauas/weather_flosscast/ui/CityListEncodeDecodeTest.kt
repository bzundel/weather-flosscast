package de.frauas.weather_flosscast.ui

import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.decodeCity
import de.frauas.weather_flosscast.encodeCity

import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import kotlin.random.Random

class CityListEncodeDecodeTest {
    val mockCities = listOf(
        City("Paris", "Île-de-France", "France", 48.8566, 2.3522),
        City("New York", "NY", "USA", 40.7128, -74.0060),
        City("Tokyo", "Tokyo", "Japan", 35.6895, 139.6917),
        City("Sydney", "NSW", "Australia", -33.8688, 151.2093),
        City("Cape Town", "Western Cape", "South Africa", -33.9249, 18.4241),
        City("Berlin", "Berlin", "Germany", 52.5200, 13.4050),
        City("São Paulo", "SP", "Brazil", -23.5505, -46.6333),
        City("Toronto", "Ontario", "Canada", 43.6510, -79.3470),
        City("Mumbai", "Maharashtra", "India", 19.0760, 72.8777),
        City("Reykjavík", "Capital Region", "Iceland", 64.1265, -21.8174)
    )

    @Test
    fun encodeCityTest(){
        val mockCity = mockCities.get(Random.nextInt(0, mockCities.size))
        val encodedTestCityJson = JSONObject(encodeCity(mockCity))

        assertEquals(encodedTestCityJson.getString("cityName"), mockCity.cityName)
        assertEquals(encodedTestCityJson.getString("state"), mockCity.state)
        assertEquals(encodedTestCityJson.getString("country"), mockCity.country)
    }

    @Test
    fun decodeCityTest(){
        val mockCity = mockCities.get(Random.nextInt(0, mockCities.size))
        val encodedTestCity = encodeCity(mockCity)
        val mockCityDecoded = decodeCity(encodedTestCity)

        assertEquals(mockCityDecoded.cityName, mockCity.cityName)
        assertEquals(mockCityDecoded.state, mockCity.state)
        assertEquals(mockCityDecoded.country, mockCity.country)
    }
}
