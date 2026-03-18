package com.example.kotlin_movie_app

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kotlin_movie_app.core.TAG
import com.example.kotlin_movie_app.core.data.UserPreferences
import com.example.kotlin_movie_app.core.data.UserPreferencesRepository
import com.example.kotlin_movie_app.comp.data.MovieRepository
import kotlinx.coroutines.launch

class MovieAppViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val movieRepository: MovieRepository) : ViewModel() {
    init { Log.d(TAG, "init") }

    fun logout() {
        viewModelScope.launch { movieRepository.deleteAll(); userPreferencesRepository.save(UserPreferences()) }
    }

    fun setToken(token: String) { movieRepository.setToken(token) }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MovieApplication)
                MovieAppViewModel(app.container.userPreferencesRepository, app.container.movieRepository)
            }
        }
    }
}

