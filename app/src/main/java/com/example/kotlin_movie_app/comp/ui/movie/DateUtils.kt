package com.example.kotlin_movie_app.comp.ui.movie

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

object DateUtils {
    fun isoToMillis(isoString: String?): Long {
        if (isoString.isNullOrEmpty()) return System.currentTimeMillis()
        return try {
            val formatter = SimpleDateFormat(ISO_PATTERN, Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) { System.currentTimeMillis() }
    }

    fun millisToIso(millis: Long): String {
        val formatter = SimpleDateFormat(ISO_PATTERN, Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(millis))
    }

    fun isoToDisplay(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return ""
        return try {
            val millis = isoToMillis(isoString)
            val displayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            displayFormatter.format(Date(millis))
        } catch (_: Exception) { isoString }
    }
}