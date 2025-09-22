package com.filmcatalog.data.model

import com.google.gson.annotations.SerializedName
import com.filmcatalog.data.service.TMDBService

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val popularity: Double,
    @SerializedName("genre_ids")
    val genreIds: List<Int> = emptyList(),
    val adult: Boolean = false,
    val video: Boolean = false,
    @SerializedName("original_language")
    val originalLanguage: String = "",
    @SerializedName("original_title")
    val originalTitle: String = ""
) {
    val posterImageUrl: String?
        get() = TMDBService.getImageUrl(posterPath)
    
    val backdropImageUrl: String?
        get() = TMDBService.getImageUrl(backdropPath)
}

data class MoviesResponse(
    val page: Int,
    val results: List<Movie>,
    val total_pages: Int,
    val total_results: Int
)

data class Genre(
    val id: Int,
    val name: String
)

data class GenresResponse(
    val genres: List<Genre>
)

data class ProductionCompany(
    val id: Int,
    val name: String,
    @SerializedName("logo_path")
    val logoPath: String?,
    @SerializedName("origin_country")
    val originCountry: String
)

data class MovieDetails(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val popularity: Double,
    val genres: List<Genre> = emptyList(),
    val adult: Boolean = false,
    val video: Boolean = false,
    @SerializedName("original_language")
    val originalLanguage: String = "",
    @SerializedName("original_title")
    val originalTitle: String = "",
    val runtime: Int? = null,
    val budget: Long = 0,
    val revenue: Long = 0,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionCompany> = emptyList(),
    val tagline: String? = null
) {
    val posterImageUrl: String?
        get() = TMDBService.getImageUrl(posterPath)
    
    val backdropImageUrl: String?
        get() = TMDBService.getImageUrl(backdropPath)
}