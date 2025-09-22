package com.filmcatalog.kmp.data.storage

import com.filmcatalog.kmp.data.model.Movie
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface MovieStorageService {
    suspend fun addToFavorites(movie: Movie)
    suspend fun removeFromFavorites(movie: Movie)
    suspend fun addToWatched(movie: Movie)
    suspend fun removeFromWatched(movie: Movie)
    suspend fun addToWatchlist(movie: Movie)
    suspend fun removeFromWatchlist(movie: Movie)
    suspend fun scheduleMovie(movie: Movie, scheduledDate: Long)
    suspend fun removeFromScheduled(movie: Movie)
    
    suspend fun getFavorites(): List<Movie>
    suspend fun getWatched(): List<Movie>
    suspend fun getWatchlist(): List<Movie>
    suspend fun getScheduled(): List<Movie>
    
    suspend fun getMoviesByStatus(status: MovieStatus): List<Movie>
    suspend fun isInFavorites(movieId: Int): Boolean
    suspend fun isWatched(movieId: Int): Boolean
    suspend fun isInWatchlist(movieId: Int): Boolean
    suspend fun isScheduled(movieId: Int): Boolean
    
    suspend fun getScheduledDateTime(movieId: Int): Long?
    
    suspend fun getFavoritesCount(): Int
    suspend fun getWatchedCount(): Int
    suspend fun getWatchlistCount(): Int
    suspend fun getScheduledCount(): Int
    
    suspend fun clearAllData()
}

// Persistent implementation 
class PersistentMovieStorageService(private val settings: Settings) : MovieStorageService {
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val MOVIES_KEY = "stored_movies"
    }
    
    private suspend fun getStoredMovies(): MutableMap<Int, StoredMovie> = withContext(Dispatchers.Default) {
        val moviesJson = settings.getStringOrNull(MOVIES_KEY) ?: return@withContext mutableMapOf()
        try {
            val moviesList = json.decodeFromString<List<StoredMovie>>(moviesJson)
            moviesList.associateBy { it.id }.toMutableMap()
        } catch (e: Exception) {
            println("Error deserializing movies: ${e.message}")
            mutableMapOf()
        }
    }
    
    private suspend fun saveStoredMovies(movies: Map<Int, StoredMovie>) = withContext(Dispatchers.Default) {
        try {
            val moviesJson = json.encodeToString(movies.values.toList())
            settings.putString(MOVIES_KEY, moviesJson)
        } catch (e: Exception) {
            println("Error serializing movies: ${e.message}")
        }
    }
    
    private fun Movie.toStoredMovie(status: MovieStatus, scheduledDate: Long? = null): StoredMovie {
        return StoredMovie(
            id = id,
            title = title,
            posterPath = posterPath ?: "",
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            overview = overview,
            backdropPath = backdropPath,
            status = status,
            scheduledDate = scheduledDate
        )
    }
    
    override suspend fun addToFavorites(movie: Movie) {
        val movies = getStoredMovies()
        movies[movie.id] = movie.toStoredMovie(MovieStatus.FAVORITE)
        saveStoredMovies(movies)
    }
    
    override suspend fun removeFromFavorites(movie: Movie) {
        val movies = getStoredMovies()
        movies.remove(movie.id)
        saveStoredMovies(movies)
    }
    
    override suspend fun addToWatched(movie: Movie) {
        val movies = getStoredMovies()
        movies[movie.id] = movie.toStoredMovie(MovieStatus.WATCHED)
        saveStoredMovies(movies)
    }
    
    override suspend fun removeFromWatched(movie: Movie) {
        val movies = getStoredMovies()
        movies.remove(movie.id)
        saveStoredMovies(movies)
    }
    
    override suspend fun addToWatchlist(movie: Movie) {
        val movies = getStoredMovies()
        movies[movie.id] = movie.toStoredMovie(MovieStatus.WATCHLIST)
        saveStoredMovies(movies)
    }
    
    override suspend fun removeFromWatchlist(movie: Movie) {
        val movies = getStoredMovies()
        movies.remove(movie.id)
        saveStoredMovies(movies)
    }
    
    override suspend fun scheduleMovie(movie: Movie, scheduledDate: Long) {
        val movies = getStoredMovies()
        movies[movie.id] = movie.toStoredMovie(MovieStatus.SCHEDULED, scheduledDate)
        saveStoredMovies(movies)
    }
    
    override suspend fun removeFromScheduled(movie: Movie) {
        val movies = getStoredMovies()
        movies.remove(movie.id)
        saveStoredMovies(movies)
    }
    
    override suspend fun getFavorites(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { it.status == MovieStatus.FAVORITE }.map { it.toMovie() }
    }
    
    override suspend fun getWatched(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { it.status == MovieStatus.WATCHED }.map { it.toMovie() }
    }
    
    override suspend fun getWatchlist(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { it.status == MovieStatus.WATCHLIST }.map { it.toMovie() }
    }
    
    override suspend fun getScheduled(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { it.status == MovieStatus.SCHEDULED }.map { it.toMovie() }
    }
    
    override suspend fun getMoviesByStatus(status: MovieStatus): List<Movie> {
        return when (status) {
            MovieStatus.FAVORITE -> getFavorites()
            MovieStatus.WATCHED -> getWatched()
            MovieStatus.WATCHLIST -> getWatchlist()
            MovieStatus.SCHEDULED -> getScheduled()
        }
    }
    
    override suspend fun isInFavorites(movieId: Int): Boolean {
        val movies = getStoredMovies()
        return movies[movieId]?.status == MovieStatus.FAVORITE
    }
    
    override suspend fun isWatched(movieId: Int): Boolean {
        val movies = getStoredMovies()
        return movies[movieId]?.status == MovieStatus.WATCHED
    }
    
    override suspend fun isInWatchlist(movieId: Int): Boolean {
        val movies = getStoredMovies()
        return movies[movieId]?.status == MovieStatus.WATCHLIST
    }
    
    override suspend fun isScheduled(movieId: Int): Boolean {
        val movies = getStoredMovies()
        return movies[movieId]?.status == MovieStatus.SCHEDULED
    }
    
    override suspend fun getScheduledDateTime(movieId: Int): Long? {
        val movies = getStoredMovies()
        return movies[movieId]?.takeIf { it.status == MovieStatus.SCHEDULED }?.scheduledDate
    }
    
    override suspend fun getFavoritesCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { it.status == MovieStatus.FAVORITE }
    }
    
    override suspend fun getWatchedCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { it.status == MovieStatus.WATCHED }
    }
    
    override suspend fun getWatchlistCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { it.status == MovieStatus.WATCHLIST }
    }
    
    override suspend fun getScheduledCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { it.status == MovieStatus.SCHEDULED }
    }
    
    override suspend fun clearAllData() {
        settings.remove(MOVIES_KEY)
    }
}

object MovieStorageManager {
    private var _instance: MovieStorageService? = null
    
    val instance: MovieStorageService
        get() = _instance ?: throw IllegalStateException("MovieStorageManager not initialized")
    
    fun initialize(settings: Settings) {
        _instance = PersistentMovieStorageService(settings)
    }
}