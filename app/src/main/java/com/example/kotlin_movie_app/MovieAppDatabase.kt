package com.example.kotlin_movie_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kotlin_movie_app.comp.data.Movie
import com.example.kotlin_movie_app.comp.data.local.MovieDao
import com.example.kotlin_movie_app.comp.data.local.MovieTypeConverters

@Database(entities = [Movie::class], version = 1)
@androidx.room.TypeConverters(MovieTypeConverters::class)
abstract class MovieAppDatabase : RoomDatabase() {
    abstract fun itemDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: MovieAppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new table
                db.execSQL("CREATE TABLE movies_new (_id TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, date TEXT NOT NULL, seen BOOLEAN NOT NULL, rating REAL NOT NULL, location TEXT NOT NULL, image TEXT NOT NULL, isSynced BOOLEAN NOT NULL, PRIMARY KEY(_id))")
                // Copy the data
                db.execSQL("INSERT INTO movies_new (_id, name, description, date, seen, rating, location, image, isSynced) SELECT _id, name, description, date, seen, rating, location, image, isSynced FROM movies")
                // Remove the old table
                db.execSQL("DROP TABLE movies")
                // Change the table name
                db.execSQL("ALTER TABLE movies_new RENAME TO movies")
            }
        }

        fun getDatabase(context: Context): MovieAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder( context,
                    MovieAppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration(true).build()
                //addMigrations(MIGRATION_1_2)
                INSTANCE = instance
                instance
            }
        }
    }
}
