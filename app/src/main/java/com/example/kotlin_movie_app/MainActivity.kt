package com.example.kotlin_movie_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.kotlin_movie_app.ui.theme.KotlinMovieAppTheme
import com.example.kotlin_movie_app.core.TAG
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Log.d(TAG, "onCreate"); MovieApp { MovieAppNavHost() } }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { (application as MovieApplication).container.movieRepository.openWsClient() }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch { (application as MovieApplication).container.movieRepository.closeWsClient() }
    }
}

@Composable
fun MovieApp(content: @Composable () -> Unit) {
    Log.d("MyApp", "recompose")
    KotlinMovieAppTheme { Surface { content() } }
}

@Preview
@Composable
fun PreviewMovieApp() { MovieApp { MovieAppNavHost() } }