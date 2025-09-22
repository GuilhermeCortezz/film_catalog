package com.filmcatalog.kmp.data.remote

import com.filmcatalog.kmp.data.model.Movie
import com.filmcatalog.kmp.data.model.MoviesResponse
import kotlinx.serialization.Serializable
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Serializable
data class GenresResponse(
    val genres: List<GenreItem>
)

@Serializable
data class GenreItem(
    val id: Int,
    val name: String
)

class TMDBService {
    private val baseUrl = "https://api.themoviedb.org/3/"
    private val apiToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiZmUwMWE1Y2ZmYzBmNTY3MDZhYmM1NmU4YzNlNzA4ZCIsIm5iZiI6MTc1ODMzNDY5Mi4xNjQsInN1YiI6IjY4Y2UwZWU0MWVjNDQzYjEwMGNkNmU1ZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.3Q1ybsIUmSvSikNED11dtMIQ-beSVXfJOMZE-jO8HKg"
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    fun getImageUrl(path: String?, size: String = "w500"): String? {
        return if (path != null) {
            "https://image.tmdb.org/t/p/$size$path"
        } else null
    }
    
    suspend fun getPopularMovies(page: Int = 1): MoviesResponse {
        return getMovies("movie/popular", page)
    }
    
    suspend fun getTopRatedMovies(page: Int = 1): MoviesResponse {
        return getMovies("movie/top_rated", page)
    }
    
    suspend fun getNowPlayingMovies(page: Int = 1): MoviesResponse {
        return getMovies("movie/now_playing", page)
    }
    
    suspend fun getUpcomingMovies(page: Int = 1): MoviesResponse {
        return getMovies("movie/upcoming", page)
    }
    
    suspend fun getGenres(): GenresResponse {
        return try {
            val response = client.get("${baseUrl}genre/movie/list") {
                header("Authorization", "Bearer $apiToken")
                header("accept", "application/json")
                parameter("language", "pt-BR")
            }
            
            response.body()
        } catch (e: Exception) {
            GenresResponse(genres = emptyList())
        }
    }
    
    suspend fun discoverMovies(
        page: Int = 1,
        withGenres: String? = null,
        sortBy: String = "popularity.desc"
    ): MoviesResponse {
        return try {
            val response = client.get("${baseUrl}discover/movie") {
                header("Authorization", "Bearer $apiToken")
                header("accept", "application/json")
                parameter("language", "pt-BR")
                parameter("page", page)
                parameter("region", "BR")
                parameter("sort_by", sortBy)
                parameter("include_adult", false)
                if (withGenres != null) {
                    parameter("with_genres", withGenres)
                }
            }
            
            val moviesResponse: MoviesResponse = response.body()
            
            // Add image URLs to movies
            val moviesWithImages = moviesResponse.results.map { movie ->
                movie.copy(
                    posterImageUrl = getImageUrl(movie.posterPath),
                    backdropImageUrl = getImageUrl(movie.backdropPath, "w780")
                )
            }
            
            moviesResponse.copy(results = moviesWithImages)
        } catch (e: Exception) {
            MoviesResponse(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            )
        }
    }
    
    suspend fun searchMovies(query: String, page: Int = 1): MoviesResponse {
        return try {
            val response = client.get("${baseUrl}search/movie") {
                header("Authorization", "Bearer $apiToken")
                header("accept", "application/json")
                parameter("language", "pt-BR")
                parameter("page", page)
                parameter("query", query)
                parameter("include_adult", false)
            }
            
            val moviesResponse: MoviesResponse = response.body()
            
            // Add image URLs to movies
            val moviesWithImages = moviesResponse.results.map { movie ->
                movie.copy(
                    posterImageUrl = getImageUrl(movie.posterPath),
                    backdropImageUrl = getImageUrl(movie.backdropPath, "w780")
                )
            }
            
            moviesResponse.copy(results = moviesWithImages)
        } catch (e: Exception) {
            MoviesResponse(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            )
        }
    }
    
    private suspend fun getMovies(endpoint: String, page: Int): MoviesResponse {
        return try {
            val response = client.get("$baseUrl$endpoint") {
                header("Authorization", "Bearer $apiToken")
                header("accept", "application/json")
                parameter("language", "pt-BR")
                parameter("page", page)
                parameter("region", "BR")
            }
            
            val moviesResponse: MoviesResponse = response.body()
            
            // Add image URLs to movies
            val moviesWithImages = moviesResponse.results.map { movie ->
                movie.copy(
                    posterImageUrl = getImageUrl(movie.posterPath),
                    backdropImageUrl = getImageUrl(movie.backdropPath, "w780")
                )
            }
            
            moviesResponse.copy(results = moviesWithImages)
        } catch (e: Exception) {
            MoviesResponse(
                page = 1,
                results = emptyList(),
                totalPages = 0,
                totalResults = 0
            )
        }
    }
}