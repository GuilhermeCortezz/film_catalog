package com.filmcatalog.data.service

import android.content.Context
import android.content.SharedPreferences
import android.content.ContentValues
import android.content.Intent
import android.provider.CalendarContract
import android.net.Uri
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.model.MovieSchedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class MovieScheduleService(private val context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("movie_schedules", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun scheduleMovie(
        movie: Movie,
        scheduledDate: Date,
        notes: String? = null,
        addToCalendar: Boolean = true
    ): MovieSchedule {
        val scheduleId = UUID.randomUUID().toString()
        val schedule = MovieSchedule(
            id = scheduleId,
            movieId = movie.id,
            movieTitle = movie.title,
            moviePoster = movie.posterPath,
            scheduledDate = scheduledDate.time,
            notes = notes
        )

        // Salva no SharedPreferences
        saveSchedule(schedule)

        // Adiciona ao calendário se solicitado
        if (addToCalendar) {
            addToCalendar(schedule)
        }

        return schedule
    }

    fun getMovieSchedule(movieId: Int): MovieSchedule? {
        val schedules = getAllSchedules()
        return schedules.find { it.movieId == movieId }
    }

    fun getAllSchedules(): List<MovieSchedule> {
        val schedulesJson = sharedPreferences.getString("schedules", "[]")
        val type = object : TypeToken<List<MovieSchedule>>() {}.type
        return gson.fromJson(schedulesJson, type) ?: emptyList()
    }

    fun removeSchedule(scheduleId: String) {
        val schedules = getAllSchedules().toMutableList()
        schedules.removeAll { it.id == scheduleId }
        saveAllSchedules(schedules)
    }

    fun removeMovieSchedule(movieId: Int) {
        val schedules = getAllSchedules().toMutableList()
        schedules.removeAll { it.movieId == movieId }
        saveAllSchedules(schedules)
    }

    fun updateSchedule(
        scheduleId: String,
        newDate: Date,
        notes: String? = null
    ): MovieSchedule? {
        val schedules = getAllSchedules().toMutableList()
        val scheduleIndex = schedules.indexOfFirst { it.id == scheduleId }
        
        if (scheduleIndex != -1) {
            val updatedSchedule = schedules[scheduleIndex].copy(
                scheduledDate = newDate.time,
                notes = notes
            )
            schedules[scheduleIndex] = updatedSchedule
            saveAllSchedules(schedules)
            
            // Atualiza no calendário
            addToCalendar(updatedSchedule)
            
            return updatedSchedule
        }
        
        return null
    }

    fun getUpcomingSchedules(hoursAhead: Int = 24): List<MovieSchedule> {
        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + (hoursAhead * 60 * 60 * 1000)
        
        return getAllSchedules()
            .filter { it.scheduledDate in currentTime..futureTime }
            .sortedBy { it.scheduledDate }
    }

    fun isMovieScheduled(movieId: Int): Boolean {
        return getMovieSchedule(movieId) != null
    }

    private fun saveSchedule(schedule: MovieSchedule) {
        val schedules = getAllSchedules().toMutableList()
        // Remove agendamento existente para o mesmo filme
        schedules.removeAll { it.movieId == schedule.movieId }
        schedules.add(schedule)
        saveAllSchedules(schedules)
    }

    private fun saveAllSchedules(schedules: List<MovieSchedule>) {
        val schedulesJson = gson.toJson(schedules)
        sharedPreferences.edit()
            .putString("schedules", schedulesJson)
            .apply()
    }

    private fun addToCalendar(schedule: MovieSchedule) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Assistir: ${schedule.movieTitle}")
                putExtra(CalendarContract.Events.DESCRIPTION, schedule.notes ?: "Lembrete para assistir o filme")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, schedule.scheduledDate)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, schedule.scheduledDate + (2 * 60 * 60 * 1000)) // 2 horas depois
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Se não conseguir abrir o calendário, ignora silenciosamente
            e.printStackTrace()
        }
    }

    fun clearAllSchedules() {
        sharedPreferences.edit()
            .remove("schedules")
            .apply()
    }
}