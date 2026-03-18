package com.example.kotlin_movie_app.auth.data

import android.util.Log
import com.example.kotlin_movie_app.auth.data.remote.AuthDataSource
import com.example.kotlin_movie_app.auth.data.remote.TokenHolder
import com.example.kotlin_movie_app.auth.data.remote.User
import com.example.kotlin_movie_app.core.TAG
import com.example.kotlin_movie_app.core.data.remote.Api

class AuthRepository(private val authDataSource: AuthDataSource) {
    init { Log.d(TAG, "init") }
    suspend fun login(username: String, password: String): Result<TokenHolder> {
        val user = User(username, password)
        val result = authDataSource.login(user)
        if (result.isSuccess) Api.tokenInterceptor.token = result.getOrNull()?.token
        return result
    }
}
