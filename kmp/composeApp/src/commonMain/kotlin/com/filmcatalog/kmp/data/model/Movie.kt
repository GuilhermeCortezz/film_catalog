package com.filmcatalog.kmp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("release_date")
    val releaseDate: String = "",
    @SerialName("vote_average")
    val voteAverage: Double = 0.0,
    @SerialName("vote_count")
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    @SerialName("genre_ids")
    val genreIds: List<Int> = emptyList(),
    val adult: Boolean = false,
    val video: Boolean = false,
    @SerialName("original_language")
    val originalLanguage: String = "",
    @SerialName("original_title")
    val originalTitle: String = "",
    
    // Campos computed - adicionados pelo service
    val posterImageUrl: String? = null,
    val backdropImageUrl: String? = null
)

@Serializable
data class MoviesResponse(
    val page: Int,
    val results: List<Movie>,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
)

@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class GenresResponse(
    val genres: List<Genre>
)