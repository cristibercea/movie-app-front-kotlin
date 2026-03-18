package com.example.kotlin_movie_app.comp.ui.movie

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.kotlin_movie_app.MovieApplication
import com.example.kotlin_movie_app.core.Result
import com.example.kotlin_movie_app.core.TAG
import com.example.kotlin_movie_app.comp.data.Movie
import com.example.kotlin_movie_app.comp.data.MovieLocation
import com.example.kotlin_movie_app.comp.data.MovieRepository
import kotlinx.coroutines.launch

data class MovieUiState(
    val itemId: String? = null,
    val movie: Movie = Movie(),
    var loadResult: Result<Movie>? = null,
    var submitResult: Result<Movie>? = null,
)

class MovieViewModel(private val movieId: String?, private val movieRepository: MovieRepository) :
    ViewModel() {

    var uiState: MovieUiState by mutableStateOf(MovieUiState(loadResult = Result.Loading))
        private set

    init {
        Log.d(TAG, "init")
        if (movieId != null) loadMovie()
        else uiState = uiState.copy(loadResult = Result.Success(Movie()))
    }

    fun loadMovie() {
        viewModelScope.launch {
            movieRepository.movieStream.collect { items ->
                if (uiState.loadResult !is Result.Loading) return@collect
                val movie = items.find { it._id == movieId } ?: Movie()
                uiState = uiState.copy(movie = movie, loadResult = Result.Success(movie))
            }
        }
    }


    fun saveOrUpdateMovie(id: String, name: String, description: String, date: String, location: MovieLocation, seen: Boolean, rating: Float, image: String) {
        viewModelScope.launch {
            Log.d(TAG, "saveOrUpdateMovie...")
            try {
                val mId: String = if (id == "none") "no_id" else id
                uiState = uiState.copy(submitResult = Result.Loading)
                val movie = uiState.movie.copy(_id = mId, name = name, description = description, date = date, location = location, seen = seen, rating = rating, image = image)
                val savedMovie: Movie = if (mId == "no_id") movieRepository.save(movie)
                                        else movieRepository.update(movie)
                Log.d(TAG, "saveOrUpdateMovie succeeded")
                uiState = uiState.copy(submitResult = Result.Success(savedMovie))
            } catch (e: Exception) {
                Log.d(TAG, "saveOrUpdateMovie failed")
                uiState = uiState.copy(submitResult = Result.Error(e))
            }
        }
    }

    companion object {
        fun Factory(itemId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MovieApplication)
                MovieViewModel(itemId, app.container.movieRepository)
            }
        }
    }
}
