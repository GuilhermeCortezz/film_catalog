package com.filmcatalog.data.service

import com.filmcatalog.data.model.MoviesResponse
import com.filmcatalog.data.model.GenresResponse
import com.filmcatalog.data.model.MovieDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("language") language: String = "pt-BR",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "BR"
    ): Response<MoviesResponse>
    
    @GET("movie/top_rated") 
    suspend fun getTopRatedMovies(
        @Query("language") language: String = "pt-BR",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "BR"
    ): Response<MoviesResponse>
    
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("language") language: String = "pt-BR",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "BR"
    ): Response<MoviesResponse>
    
    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("language") language: String = "pt-BR",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "BR"
    ): Response<MoviesResponse>
    
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "pt-BR"
    ): Response<MoviesResponse>
    
    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("language") language: String = "pt-BR"
    ): Response<GenresResponse>
    
    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "pt-BR"
    ): Response<MoviesResponse>
    
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "pt-BR"
    ): Response<MovieDetails>
}