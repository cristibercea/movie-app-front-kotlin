package com.example.kotlin_movie_app.comp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kotlin_movie_app.comp.data.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM Movies")
    fun getAll(): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movies: List<Movie>)

    @Update
    suspend fun update(movie: Movie): Int

    @Query("DELETE FROM Movies WHERE _id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM Movies")
    suspend fun deleteAll()

    @Query("DELETE FROM movies WHERE isSynced = 1")
    suspend fun clearSyncedMovies()

    @Query("SELECT * FROM Movies WHERE isSynced = 0")
    suspend fun getUnsyncedMovies(): List<Movie>

    @Query("UPDATE Movies SET isSynced = 1 WHERE _id = :id")
    suspend fun markAsSynced(id: String)
}
