package com.example.kotlin_movie_app.comp.ui.movies

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kotlin_movie_app.MovieApplication
import com.example.kotlin_movie_app.comp.data.Movie
import com.example.kotlin_movie_app.comp.data.MovieRepository
import com.example.kotlin_movie_app.core.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MoviesViewModel(private val movieRepository: MovieRepository) : ViewModel() {
    val uiState: Flow<List<Movie>> = movieRepository.movieStream

    init { Log.d(TAG, "init"); loadItems() }

    fun loadItems() {
        Log.d(TAG, "loadItems...")
        viewModelScope.launch {
            try { movieRepository.refresh() }
            catch (e: Exception) { Log.e(TAG, "Failed to refresh movies", e) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MovieApplication)
                MoviesViewModel(app.container.movieRepository)
            }
        }
    }
}