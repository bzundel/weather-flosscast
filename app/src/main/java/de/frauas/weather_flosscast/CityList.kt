package de.frauas.weather_flosscast

import android.content.Context
import android.util.Base64
import android.widget.Toast
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json

//Singleton Object CityList
// Creating PREF file for saving city names and coordinates in XLM-File(SharedPreferences File)
object CityList {
    private const val PREF_NAME = "CityListPref"    //SharedPreferences file name and Key name
    private const val CITY_LIST_KEY = "cityList"
    private val gson = Gson()

    /** NOT NEEDED CITY LIST IS A SINGLETON
     * Initializes the city list only if it doesn't already exist.
    fun initializeCitiesIfEmpty(context: Context): City {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(CITY_LIST_KEY)) {
            return City("Empty", 0.0, 0.0)
        }else{
            val currentList = getCities(context).toMutableList()
            return currentList.first()
        }
    }
     */
    //used for changing whole City List
    fun saveCities(context: Context, cities: List<City>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            val json = gson.toJson(cities)  //Convert json file
            putString(CITY_LIST_KEY, json)
        }
    }

    // Retrieve list of cities
    fun getCities(context: Context): List<City> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(CITY_LIST_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<City>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
    /**
     * Adds a new city to the list.
     * If the city already exists (same name), it replaces the old entry.
     * If the list exceeds 25 items, it removes the oldest city.
     */
    fun addCity(context: Context, newCity: City) {
        val currentList = getCities(context).toMutableList()

        // Check if the city already exists by name
        val existingIndex = currentList.indexOfFirst { it.cityName == newCity.cityName }

        if (existingIndex >= 0) {
            //Toast.makeText(context, "Ort '${newCity.cityName}' existiert bereits.", Toast.LENGTH_SHORT).show() //User notification --> Debugging

            // Replace existing city with updated data
            currentList.removeAt(existingIndex) //Changing the index of the city to 0 so that will be shown as first on the list
            currentList.add(0, newCity)

        } else {
            // If more than 25, remove the oldest city
            if (currentList.size >= 25) {
                currentList.removeLast()
            }
            currentList.add(0, newCity) // Add new city to the end
        }

        // Save updated list back to SharedPreferences
        saveCities(context, currentList)
    }

    /**
     * Removes a city from the list by its name.
     * If the city is not found, the list remains unchanged.
     */
    fun removeCityByName(context: Context, cityName: String) {
        val currentList = getCities(context).toMutableList()

        // Remove the city with the matching name
        val updatedList = currentList.filter { it.cityName != cityName }

        // Save the updated list back to SharedPreferences
        saveCities(context, updatedList)
    }

    /**
     * Clears all stored cities from SharedPreferences.
     * Useful for debugging or reset functionality.
     */
    fun clearCities(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                remove(CITY_LIST_KEY)
            }
    }
}
fun encodeCity(city: City): String {
    val json = Json.encodeToString(city)
    return Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
}

fun decodeCity(encoded: String): City {
    val json = String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
    return Json.decodeFromString(json)
}