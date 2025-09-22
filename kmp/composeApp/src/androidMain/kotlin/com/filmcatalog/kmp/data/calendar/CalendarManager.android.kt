package com.filmcatalog.kmp.data.calendar

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.filmcatalog.kmp.data.model.Movie
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.util.*

actual class CalendarManager(private val context: Context) {
    
    actual suspend fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    actual suspend fun requestCalendarPermission(): Boolean {
        // This should be handled by the calling Activity/Fragment
        // Return current permission status
        return hasCalendarPermission()
    }
    
    actual suspend fun scheduleMovie(movie: Movie, dateTime: LocalDateTime): Boolean {
        if (!hasCalendarPermission()) {
            return false
        }
        
        try {
            // Convert LocalDateTime to milliseconds
            val startMillis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = startMillis + (2 * 60 * 60 * 1000) // 2 hours duration
            
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, "Assistir: ${movie.title}")
                put(CalendarContract.Events.DESCRIPTION, buildDescription(movie))
                put(CalendarContract.Events.CALENDAR_ID, getDefaultCalendarId())
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.currentSystemDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }
            
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            
            // Add reminder 30 minutes before
            if (uri != null) {
                val eventId = uri.lastPathSegment?.toLong()
                if (eventId != null) {
                    addReminder(eventId, 30) // 30 minutes before
                }
            }
            
            return uri != null
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    private fun buildDescription(movie: Movie): String {
        return buildString {
            appendLine("Assistir: ${movie.title}")
            if (movie.overview.isNotEmpty()) {
                appendLine()
                appendLine("Sinopse:")
                appendLine(movie.overview)
            }
            if (movie.releaseDate.isNotEmpty()) {
                appendLine()
                appendLine("Lançamento: ${movie.releaseDate}")
            }
            if (movie.voteAverage > 0) {
                appendLine("Avaliação: ${String.format("%.1f", movie.voteAverage)}/10")
            }
            appendLine()
            appendLine("Agendado pelo Film Catalog")
        }
    }
    
    private fun getDefaultCalendarId(): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1 AND ${CalendarContract.Calendars.IS_PRIMARY} = 1",
            null,
            null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        
        // Fallback: get first available calendar
        val fallbackCursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )
        
        fallbackCursor?.use {
            if (it.moveToFirst()) {
                return it.getLong(0)
            }
        }
        
        return 1L // Default fallback
    }
    
    private fun addReminder(eventId: Long, minutesBefore: Int) {
        try {
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventId)
                put(CalendarContract.Reminders.MINUTES, minutesBefore)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            }
            
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}