package com.example.kotlin_movie_app.comp.data.remote

import com.example.kotlin_movie_app.comp.data.Movie

data class MovieEvent(val type: String, val payload: Movie)
