package com.example.kotlin_movie_app

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotlin_movie_app.auth.LoginScreen
import com.example.kotlin_movie_app.core.data.UserPreferences
import com.example.kotlin_movie_app.core.data.remote.Api
import com.example.kotlin_movie_app.core.ui.UserPreferencesViewModel
import com.example.kotlin_movie_app.comp.ui.movie.MovieScreen
import com.example.kotlin_movie_app.comp.ui.movies.MoviesScreen

const val itemsRoute = "items"
const val authRoute = "auth"

@Composable
fun MovieAppNavHost() {
    val navController = rememberNavController()
    val onCloseItem = { Log.d("MyAppNavHost", "navigate back to list"); navController.popBackStack() }
    val userPreferencesViewModel = viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesUiState by userPreferencesViewModel.uiState.collectAsStateWithLifecycle(initialValue = UserPreferences())
    val movieAppViewModel = viewModel<MovieAppViewModel>(factory = MovieAppViewModel.Factory)

    NavHost(navController = navController, startDestination = authRoute) {
        composable(itemsRoute) {
            MoviesScreen(
                onItemClick = { itemId ->
                    Log.d("MyAppNavHost", "navigate to item $itemId")
                    navController.navigate("$itemsRoute/$itemId")
                },
                onAddItem = {
                    Log.d("MyAppNavHost", "navigate to new item")
                    navController.navigate("$itemsRoute-new")
                },
                onLogout = {
                    Log.d("MyAppNavHost", "logout")
                    movieAppViewModel.logout()
                    Api.tokenInterceptor.token = null
                    navController.navigate(authRoute) { popUpTo(0) }
                })
        }

        composable( route = "$itemsRoute/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { MovieScreen(itemId = it.arguments?.getString("id")?:"none", onClose = { onCloseItem() }) }

        composable(route = "$itemsRoute-new") { MovieScreen(itemId = "none", onClose = { onCloseItem() }) }

        composable(route = authRoute) {
            LoginScreen(
                onClose = {
                    Log.d("MyAppNavHost", "navigate to list")
                    navController.navigate(itemsRoute)
                }
            )
        }
    }
    LaunchedEffect(userPreferencesUiState.token) {
        if (userPreferencesUiState.token.isNotEmpty()) {
            Log.d("MyAppNavHost", "Launched effect navigate to items")
            Api.tokenInterceptor.token = userPreferencesUiState.token
            movieAppViewModel.setToken(userPreferencesUiState.token)
            navController.navigate(itemsRoute) { popUpTo(0) }
        }
    }
}
