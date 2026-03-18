package com.example.kotlin_movie_app

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.example.kotlin_movie_app.auth.data.AuthRepository
import com.example.kotlin_movie_app.auth.data.remote.AuthDataSource
import com.example.kotlin_movie_app.core.TAG
import com.example.kotlin_movie_app.core.data.UserPreferencesRepository
import com.example.kotlin_movie_app.core.data.remote.Api
import com.example.kotlin_movie_app.comp.data.MovieRepository
import com.example.kotlin_movie_app.comp.data.remote.MovieService
import com.example.kotlin_movie_app.comp.data.remote.WSClient

val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

class AppContainer(val context: Context) {
    init { Log.d(TAG, "init") }
    private val movieService: MovieService = Api.retrofit.create(MovieService::class.java)
    private val wsClient: WSClient = WSClient(Api.okHttpClient)
    private val authDataSource: AuthDataSource = AuthDataSource()
    private val database: MovieAppDatabase by lazy { MovieAppDatabase.getDatabase(context) }
    val movieRepository: MovieRepository by lazy { MovieRepository(movieService, wsClient, database.itemDao(), context) }
    val authRepository: AuthRepository by lazy { AuthRepository(authDataSource) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(context.userPreferencesDataStore) }
}
