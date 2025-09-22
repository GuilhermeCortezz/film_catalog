package com.filmcatalog.kmp.data.storage

import com.filmcatalog.kmp.data.model.Movie
import kotlinx.serialization.Serializable

@Serializable
enum class MovieStatus {
    FAVORITE,
    WATCHED,
    WATCHLIST,
    SCHEDULED
}

@Serializable
data class StoredMovie(
    val id: Int,
    val title: String,
    val posterPath: String,
    val releaseDate: String,
    val voteAverage: Double,
    val overview: String,
    val backdropPath: String? = null,
    val statuses: Set<MovieStatus> = emptySet(),
    val addedAt: Long = System.currentTimeMillis(),
    val scheduledDate: Long? = null
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            posterPath = posterPath.takeIf { it.isNotEmpty() },
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            overview = overview,
            backdropPath = backdropPath,
            originalTitle = title, // Using title as fallback
            originalLanguage = "en", // Default value
            popularity = 0.0, // Default value
            adult = false, // Default value
            video = false, // Default value
            genreIds = emptyList() // Default value
        )
    }
}