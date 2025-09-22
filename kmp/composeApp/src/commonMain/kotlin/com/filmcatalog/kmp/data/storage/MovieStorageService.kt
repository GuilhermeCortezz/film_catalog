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
            println("Error deserializing movies, trying migration: ${e.message}")
            // Try to migrate from old format
            try {
                @Serializable
                data class OldStoredMovie(
                    val id: Int,
                    val title: String,
                    val posterPath: String,
                    val releaseDate: String,
                    val voteAverage: Double,
                    val overview: String,
                    val backdropPath: String? = null,
                    val status: MovieStatus,
                    val addedAt: Long = System.currentTimeMillis(),
                    val scheduledDate: Long? = null
                )
                
                val oldMoviesList = json.decodeFromString<List<OldStoredMovie>>(moviesJson)
                val migratedMovies = oldMoviesList.map { oldMovie ->
                    StoredMovie(
                        id = oldMovie.id,
                        title = oldMovie.title,
                        posterPath = oldMovie.posterPath,
                        releaseDate = oldMovie.releaseDate,
                        voteAverage = oldMovie.voteAverage,
                        overview = oldMovie.overview,
                        backdropPath = oldMovie.backdropPath,
                        statuses = setOf(oldMovie.status),
                        addedAt = oldMovie.addedAt,
                        scheduledDate = oldMovie.scheduledDate
                    )
                }
                
                // Save migrated data
                val migratedMap = migratedMovies.associateBy { it.id }.toMutableMap()
                saveStoredMovies(migratedMap)
                println("Successfully migrated ${migratedMovies.size} movies to new format")
                migratedMap
            } catch (migrationError: Exception) {
                println("Migration failed: ${migrationError.message}")
                mutableMapOf()
            }
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
            statuses = setOf(status),
            scheduledDate = scheduledDate
        )
    }
    
    private fun Movie.toStoredMovieWithStatuses(statuses: Set<MovieStatus>, scheduledDate: Long? = null): StoredMovie {
        return StoredMovie(
            id = id,
            title = title,
            posterPath = posterPath ?: "",
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            overview = overview,
            backdropPath = backdropPath,
            statuses = statuses,
            scheduledDate = scheduledDate
        )
    }

    override suspend fun addToFavorites(movie: Movie) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses + MovieStatus.FAVORITE
            movies[movie.id] = existingMovie.copy(statuses = newStatuses)
        } else {
            movies[movie.id] = movie.toStoredMovie(MovieStatus.FAVORITE)
        }
        saveStoredMovies(movies)
    }

    override suspend fun removeFromFavorites(movie: Movie) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses - MovieStatus.FAVORITE
            if (newStatuses.isEmpty()) {
                movies.remove(movie.id)
            } else {
                movies[movie.id] = existingMovie.copy(statuses = newStatuses)
            }
            saveStoredMovies(movies)
        }
    }

    override suspend fun addToWatched(movie: Movie) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses + MovieStatus.WATCHED
            movies[movie.id] = existingMovie.copy(statuses = newStatuses)
        } else {
            movies[movie.id] = movie.toStoredMovie(MovieStatus.WATCHED)
        }
        saveStoredMovies(movies)
    }

    override suspend fun removeFromWatched(movie: Movie) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses - MovieStatus.WATCHED
            if (newStatuses.isEmpty()) {
                movies.remove(movie.id)
            } else {
                movies[movie.id] = existingMovie.copy(statuses = newStatuses)
            }
            saveStoredMovies(movies)
        }
    }

    override suspend fun addToWatchlist(movie: Movie) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses + MovieStatus.WATCHLIST
            movies[movie.id] = existingMovie.copy(statuses = newStatuses)
        } else {
            movies[movie.id] = movie.toStoredMovie(MovieStatus.WATCHLIST)
        }
        saveStoredMovies(movies)
    }

    override suspend fun removeFromWatchlist(movie: Movie) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses - MovieStatus.WATCHLIST
            if (newStatuses.isEmpty()) {
                movies.remove(movie.id)
            } else {
                movies[movie.id] = existingMovie.copy(statuses = newStatuses)
            }
            saveStoredMovies(movies)
        }
    }

    override suspend fun scheduleMovie(movie: Movie, scheduledDate: Long) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses + MovieStatus.SCHEDULED
            movies[movie.id] = existingMovie.copy(statuses = newStatuses, scheduledDate = scheduledDate)
        } else {
            movies[movie.id] = movie.toStoredMovie(MovieStatus.SCHEDULED, scheduledDate)
        }
        saveStoredMovies(movies)
    }

    override suspend fun removeFromScheduled(movie: Movie) {
        val movies = getStoredMovies()
        val existingMovie = movies[movie.id]
        if (existingMovie != null) {
            val newStatuses = existingMovie.statuses - MovieStatus.SCHEDULED
            if (newStatuses.isEmpty()) {
                movies.remove(movie.id)
            } else {
                movies[movie.id] = existingMovie.copy(statuses = newStatuses, scheduledDate = null)
            }
            saveStoredMovies(movies)
        }
    }
    
    override suspend fun getFavorites(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { MovieStatus.FAVORITE in it.statuses }.map { it.toMovie() }
    }
    
    override suspend fun getWatched(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { MovieStatus.WATCHED in it.statuses }.map { it.toMovie() }
    }
    
    override suspend fun getWatchlist(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { MovieStatus.WATCHLIST in it.statuses }.map { it.toMovie() }
    }
    
    override suspend fun getScheduled(): List<Movie> {
        val movies = getStoredMovies()
        return movies.values.filter { MovieStatus.SCHEDULED in it.statuses }.map { it.toMovie() }
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
        return movies[movieId]?.let { MovieStatus.FAVORITE in it.statuses } ?: false
    }
    
    override suspend fun isWatched(movieId: Int): Boolean {
        val movies = getStoredMovies()
        return movies[movieId]?.let { MovieStatus.WATCHED in it.statuses } ?: false
    }
    
    override suspend fun isInWatchlist(movieId: Int): Boolean {
        val movies = getStoredMovies()
        return movies[movieId]?.let { MovieStatus.WATCHLIST in it.statuses } ?: false
    }
    
    override suspend fun isScheduled(movieId: Int): Boolean {
        val movies = getStoredMovies()
        return movies[movieId]?.let { MovieStatus.SCHEDULED in it.statuses } ?: false
    }
    
    override suspend fun getScheduledDateTime(movieId: Int): Long? {
        val movies = getStoredMovies()
        return movies[movieId]?.takeIf { MovieStatus.SCHEDULED in it.statuses }?.scheduledDate
    }
    
    override suspend fun getFavoritesCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { MovieStatus.FAVORITE in it.statuses }
    }
    
    override suspend fun getWatchedCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { MovieStatus.WATCHED in it.statuses }
    }
    
    override suspend fun getWatchlistCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { MovieStatus.WATCHLIST in it.statuses }
    }
    
    override suspend fun getScheduledCount(): Int {
        val movies = getStoredMovies()
        return movies.values.count { MovieStatus.SCHEDULED in it.statuses }
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