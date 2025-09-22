package com.filmcatalog.kmp.data.calendar

import com.filmcatalog.kmp.data.model.Movie
import kotlinx.datetime.LocalDateTime

data class CalendarEvent(
    val title: String,
    val description: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime
)

expect class CalendarManager {
    suspend fun scheduleMovie(movie: Movie, dateTime: LocalDateTime): Boolean
    suspend fun hasCalendarPermission(): Boolean
    suspend fun requestCalendarPermission(): Boolean
}