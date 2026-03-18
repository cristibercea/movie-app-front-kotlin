package com.example.kotlin_movie_app.comp.data.local

import androidx.room.TypeConverter
import org.json.JSONObject
import com.example.kotlin_movie_app.comp.data.MovieLocation

class MovieTypeConverters {
    @TypeConverter
    fun fromLocation(location: MovieLocation): String {
        val json = JSONObject()
        json.put("lat", location.lat); json.put("lng", location.lng)
        return json.toString()
    }

    @TypeConverter
    fun toLocation(jsonString: String): MovieLocation {
        val json = JSONObject(jsonString)
        return MovieLocation(lat = json.getDouble("lat"), lng = json.getDouble("lng"))
    }
}