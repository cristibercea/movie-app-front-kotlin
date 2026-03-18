package com.example.kotlin_movie_app.comp.data

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kotlin_movie_app.core.TAG
import com.example.kotlin_movie_app.core.data.remote.Api
import com.example.kotlin_movie_app.comp.data.local.MovieDao
import com.example.kotlin_movie_app.comp.data.remote.MovieEvent
import com.example.kotlin_movie_app.comp.data.remote.MovieService
import com.example.kotlin_movie_app.comp.data.remote.WSClient
import com.example.kotlin_movie_app.core.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MovieRepository(
    private val movieService: MovieService,
    private val wsClient: WSClient,
    private val movieDao: MovieDao,
    private val context: Context
) {
    val movieStream by lazy { movieDao.getAll() }

    init { Log.d(TAG, "init") }

    private fun getBearerToken() = "Bearer ${Api.tokenInterceptor.token}"

    suspend fun refresh() {
        Log.d(TAG, "refresh started")
        try {
            val movies = movieService.find(authorization = getBearerToken())
            movieDao.clearSyncedMovies()
            movieDao.insert(movies)
            Log.d(TAG, "refresh succeeded")
        } catch (e: Exception) { Log.w(TAG, "refresh failed", e) }
    }

    suspend fun openWsClient() {
        Log.d(TAG, "openWsClient")
        withContext(Dispatchers.IO) {
            getMovieEvents().collect {
                Log.d(TAG, "Item event collected $it")
                if (it.isSuccess) {
                    val movieEvent = it.getOrNull()
                    when (movieEvent?.type) {
                        "created" -> handleMovieCreated(movieEvent.payload)
                        "updated" -> handleMovieUpdated(movieEvent.payload)
                        "deleted" -> handleMovieDeleted(movieEvent.payload)
                    }
                }
            }
        }
    }

    suspend fun closeWsClient() {
        Log.d(TAG, "closeWsClient")
        withContext(Dispatchers.IO) { wsClient.closeSocket() }
    }

    fun getMovieEvents(): Flow<Result<MovieEvent>> = callbackFlow {
        Log.d(TAG, "getMovieEvents started")
        wsClient.openSocket(
            onEvent = {
                Log.d(TAG, "onEvent $it")
                if (it != null) trySend(Result.success(it))
            },
            onClosed = { close() },
            onFailure = { close() })
        awaitClose { wsClient.closeSocket() }
    }

    suspend fun update(movie: Movie): Movie {
        Log.d(TAG, "update $movie...")
        return try {
            val updatedMovie = movieService.update(itemId = movie._id, movie = movie, authorization = getBearerToken())
            val movieToSave = updatedMovie.copy(isSynced = true)
            handleMovieUpdated(movieToSave)
            Log.d(TAG, "update succeeded online")
            updatedMovie
        } catch (e: Exception) {
            Log.w(TAG, "update failed online, falling back to OFFLINE. Error: ${e.message}")
            val offlineMovie = movie.copy(
                _id = movie._id.ifEmpty { java.util.UUID.randomUUID().toString() },
                isSynced = false
            )
            handleMovieUpdated(offlineMovie)
            scheduleBackgroundSync()
            NotificationUtils.showNotification(
                context,
                "Movie saved Offline",
                "No internet. Movie changes were saved locally and will sync later.",
                isSuccess = false
            )
            offlineMovie
        }
    }

    suspend fun save(movie: Movie): Movie {
        Log.d(TAG, "save $movie...")
        return try {
            val createdItem = movieService.create(movie = movie, authorization = getBearerToken())
            val movieToSave = createdItem.copy(isSynced = true)
            handleMovieCreated(movieToSave)
            Log.d(TAG, "save succeeded online")
            createdItem
        } catch (e: Exception) {
            Log.w(TAG, "Save failed online, falling back to OFFLINE. Error: ${e.message}")
            val offlineMovie = movie.copy(
                _id = movie._id.ifEmpty { java.util.UUID.randomUUID().toString() },
                isSynced = false
            )
            handleMovieCreated(offlineMovie)
            scheduleBackgroundSync()
            NotificationUtils.showNotification(
                context,
                "Movie saved Offline",
                "No internet. New movie was saved locally and will sync later.",
                isSuccess = false
            )
            offlineMovie
        }
    }
    suspend fun syncOneMovie(movie: Movie) {
        val result = movieService.create(getBearerToken(), movie)
        movieDao.deleteById(movie._id)
        movieDao.insert(result.copy(isSynced = true))
    }

    suspend fun getUnsyncedLocalMovies(): List<Movie> { return movieDao.getUnsyncedMovies() }

    private fun scheduleBackgroundSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "movie_sync_work",
            ExistingWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun handleMovieDeleted(movie: Movie) {
        Log.d(TAG, "handleMovieDeleted - todo $movie")
    }

    private suspend fun handleMovieUpdated(movie: Movie) {
        Log.d(TAG, "handleMovieUpdated...")
        movieDao.update(movie)
    }

    private suspend fun handleMovieCreated(movie: Movie) {
        Log.d(TAG, "handleMovieCreated...")
        movieDao.insert(movie)
    }

    suspend fun deleteAll() { movieDao.deleteAll() }

    fun setToken(token: String) { wsClient.authorize(token) }
}