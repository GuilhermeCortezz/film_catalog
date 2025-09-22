package com.filmcatalog.data.model

import com.google.gson.annotations.SerializedName

data class MovieSchedule(
    val id: String,
    @SerializedName("movie_id")
    val movieId: Int,
    @SerializedName("movie_title")
    val movieTitle: String,
    @SerializedName("movie_poster")
    val moviePoster: String?,
    @SerializedName("scheduled_date")
    val scheduledDate: Long, // timestamp
    val notes: String? = null,
    @SerializedName("calendar_event_id")
    val calendarEventId: Long? = null,
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)