package de.frauas.weather_flosscast.ui

import de.frauas.weather_flosscast.City
import de.frauas.weather_flosscast.decodeCity
import de.frauas.weather_flosscast.encodeCity

import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.*
import kotlin.random.Random

//Tests correctness of City List Encoding and Decoding from String to Json and the other way around
class CityListEncodeDecodeTest {
    //List of city mocks
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

    //Test the encoding to Json
    @Test
    fun encodeCityTest(){
        //chooses random city from city mock list
        val mockCity = mockCities.get(Random.nextInt(0, mockCities.size))
        //Runs encode city function which return a string in json format
        //Cast the String to JSONObject for testing later
        val encodedTestCityJson = JSONObject(encodeCity(mockCity))

        //Asserts the values of the JSONObject are the same as the values of the chosen mock city
        assertEquals(encodedTestCityJson.getString("cityName"), mockCity.cityName)
        assertEquals(encodedTestCityJson.getString("state"), mockCity.state)
        assertEquals(encodedTestCityJson.getString("country"), mockCity.country)
    }

    //Test the decoding of the json String
    @Test
    fun decodeCityTest(){
        //Choose random city from city mock list
        val mockCity = mockCities.get(Random.nextInt(0, mockCities.size))
        //Encodes city to json string
        val encodedTestCity = encodeCity(mockCity)
        //Runs the decode function
        val mockCityDecoded = decodeCity(encodedTestCity)

        //Checks the decoded values with the chosen mock city values
        assertEquals(mockCityDecoded.cityName, mockCity.cityName)
        assertEquals(mockCityDecoded.state, mockCity.state)
        assertEquals(mockCityDecoded.country, mockCity.country)
    }
}
