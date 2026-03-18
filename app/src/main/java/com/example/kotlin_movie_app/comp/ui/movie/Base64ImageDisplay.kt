package com.example.kotlin_movie_app.comp.ui.movie

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Base64ImageDisplay(base64String: String, modifier: Modifier = Modifier) {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = base64String) {
        value = if (base64String.isNotEmpty() && base64String != "-") {
            withContext(Dispatchers.IO) {
                try {
                    val pureBase64 = base64String.substringAfter(",", base64String)
                    val decodedBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                } catch (_: Exception) { null }
            }
        } else null
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap!!,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier.background(Color.LightGray), contentAlignment = Alignment.Center) { Text("🎬") }
    }
}