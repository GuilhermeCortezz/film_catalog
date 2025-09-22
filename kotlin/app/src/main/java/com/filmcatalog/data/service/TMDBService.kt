package com.filmcatalog.data.service

import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.model.MoviesResponse
import com.filmcatalog.data.model.Genre
import com.filmcatalog.data.model.MovieDetails
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TMDBService {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val API_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiZmUwMWE1Y2ZmYzBmNTY3MDZhYmM1NmU4YzNlNzA4ZCIsIm5iZiI6MTc1ODMzNDY5Mi4xNjQsInN1YiI6IjY4Y2UwZWU0MWVjNDQzYjEwMGNkNmU1ZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.3Q1ybsIUmSvSikNED11dtMIQ-beSVXfJOMZE-jO8HKg"
    
    fun getImageUrl(path: String?, size: String = "w500"): String? {
        return if (path != null) {
            "https://image.tmdb.org/t/p/$size$path"
        } else null
    }
    
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $API_TOKEN")
            .addHeader("accept", "application/json")
            .build()
        chain.proceed(request)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(TMDBApi::class.java)
    
    suspend fun getPopularMoviesPaginated(page: Int = 1): MoviesResponse {
        return try {
            val response = api.getPopularMovies(page = page)
            if (response.isSuccessful) {
                response.body() ?: MoviesResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
            } else {
                MoviesResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MoviesResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }
    
    suspend fun getMoviesByGenrePaginated(genreId: Int, page: Int = 1): MoviesResponse {
        return try {
            val response = api.getMoviesByGenre(genreId = genreId, page = page)
            if (response.isSuccessful) {
                response.body() ?: MoviesResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
            } else {
                MoviesResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MoviesResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }
    
    suspend fun getPopularMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getPopularMovies(page = page)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getTopRatedMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getTopRatedMovies(page = page)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getNowPlayingMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getNowPlayingMovies(page = page)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getUpcomingMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getUpcomingMovies(page = page)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun searchMovies(query: String, page: Int = 1): List<Movie> {
        return try {
            val response = api.searchMovies(query = query, page = page)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getGenres(): List<Genre> {
        return try {
            val response = api.getGenres()
            if (response.isSuccessful) {
                response.body()?.genres ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getMoviesByGenre(genreId: Int, page: Int = 1): List<Movie> {
        return try {
            val response = api.getMoviesByGenre(genreId = genreId, page = page)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getMovieDetails(movieId: Int): MovieDetails? {
        return try {
            val response = api.getMovieDetails(movieId = movieId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}