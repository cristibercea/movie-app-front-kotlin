package com.example.kotlin_movie_app.comp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Transient

@Parcelize
data class MovieLocation(
    val lat: Double,
    val lng: Double
) : android.os.Parcelable

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val _id: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val seen: Boolean = false,
    val rating: Float = 0.0f,
    val location: MovieLocation = MovieLocation(0.0, 0.0),
    val image: String = "",
    @Transient
    val isSynced: Boolean = true
)
