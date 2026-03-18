package com.example.kotlin_movie_app.comp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kotlin_movie_app.comp.data.Movie

@Database(entities = [Movie::class], version = 1)
@androidx.room.TypeConverters(MovieTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun itemDao(): MovieDao
}
