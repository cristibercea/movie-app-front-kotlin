package com.example.kotlin_movie_app.comp.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kotlin_movie_app.MovieApplication
import com.example.kotlin_movie_app.core.NotificationUtils
import com.example.kotlin_movie_app.core.TAG

class SyncWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MovieApplication
        val repo = app.container.movieRepository
        return try {
            Log.d(TAG, "SyncWorker: Checking for unsynced movies...")
            val unsyncedMovies = repo.getUnsyncedLocalMovies()
            if (unsyncedMovies.isEmpty()) {
                Log.d(TAG, "SyncWorker: Nothing to sync.")
                repo.refresh()
                return Result.success()
            }
            unsyncedMovies.forEach { movie ->
                Log.d(TAG, "SyncWorker: Syncing movie ${movie.name}")
                repo.syncOneMovie(movie)
            }
            Log.d(TAG, "SyncWorker: Sync complete!")
            repo.refresh()
            NotificationUtils.showNotification(
                applicationContext,
                "Sync Complete",
                "${unsyncedMovies.size} movies synced to server!",
                isSuccess = true
            )
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker failed, retrying...", e)
            Result.retry()
        }
    }
}