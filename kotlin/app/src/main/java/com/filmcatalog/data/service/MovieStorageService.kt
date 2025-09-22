package com.filmcatalog.data.service

import android.content.Context
import android.content.SharedPreferences
import com.filmcatalog.data.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class MovieStorage(
    val movie: Movie,
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val isWatchlist: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

class MovieStorageService(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("movie_storage", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_MOVIES = "stored_movies"
    }
    
    private fun getStoredMovies(): Map<Int, MovieStorage> {
        val json = prefs.getString(KEY_MOVIES, null) ?: return emptyMap()
        val type = object : TypeToken<Map<Int, MovieStorage>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
    
    private fun saveStoredMovies(movies: Map<Int, MovieStorage>) {
        val json = gson.toJson(movies)
        prefs.edit().putString(KEY_MOVIES, json).apply()
    }
    
    fun toggleFavorite(movie: Movie): Boolean {
        val storedMovies = getStoredMovies().toMutableMap()
        val movieId = movie.id
        val currentStorage = storedMovies[movieId] ?: MovieStorage(movie)
        val newStorage = currentStorage.copy(isFavorite = !currentStorage.isFavorite)
        
        if (newStorage.isFavorite || newStorage.isWatched || newStorage.isWatchlist) {
            storedMovies[movieId] = newStorage
        } else {
            storedMovies.remove(movieId)
        }
        
        saveStoredMovies(storedMovies)
        return newStorage.isFavorite
    }
    
    fun toggleWatched(movie: Movie): Boolean {
        val storedMovies = getStoredMovies().toMutableMap()
        val movieId = movie.id
        val currentStorage = storedMovies[movieId] ?: MovieStorage(movie)
        val newStorage = currentStorage.copy(isWatched = !currentStorage.isWatched)
        
        if (newStorage.isFavorite || newStorage.isWatched || newStorage.isWatchlist) {
            storedMovies[movieId] = newStorage
        } else {
            storedMovies.remove(movieId)
        }
        
        saveStoredMovies(storedMovies)
        return newStorage.isWatched
    }
    
    fun toggleWatchlist(movie: Movie): Boolean {
        val storedMovies = getStoredMovies().toMutableMap()
        val movieId = movie.id
        val currentStorage = storedMovies[movieId] ?: MovieStorage(movie)
        val newStorage = currentStorage.copy(isWatchlist = !currentStorage.isWatchlist)
        
        if (newStorage.isFavorite || newStorage.isWatched || newStorage.isWatchlist) {
            storedMovies[movieId] = newStorage
        } else {
            storedMovies.remove(movieId)
        }
        
        saveStoredMovies(storedMovies)
        return newStorage.isWatchlist
    }
    
    fun isFavorite(movieId: Int): Boolean {
        return getStoredMovies()[movieId]?.isFavorite ?: false
    }
    
    fun isWatched(movieId: Int): Boolean {
        return getStoredMovies()[movieId]?.isWatched ?: false
    }
    
    fun isInWatchlist(movieId: Int): Boolean {
        return getStoredMovies()[movieId]?.isWatchlist ?: false
    }
    
    fun getFavoriteMovies(): List<Movie> {
        return getStoredMovies().values
            .filter { it.isFavorite }
            .sortedByDescending { it.addedAt }
            .map { it.movie }
    }
    
    fun getWatchedMovies(): List<Movie> {
        return getStoredMovies().values
            .filter { it.isWatched }
            .sortedByDescending { it.addedAt }
            .map { it.movie }
    }
    
    fun getWatchlistMovies(): List<Movie> {
        return getStoredMovies().values
            .filter { it.isWatchlist }
            .sortedByDescending { it.addedAt }
            .map { it.movie }
    }
    
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
    
    fun clearFavorites() {
        val storedMovies = getStoredMovies().toMutableMap()
        val keysToRemove = mutableListOf<Int>()
        val moviesToUpdate = mutableMapOf<Int, MovieStorage>()
        
        for ((movieId, storage) in storedMovies) {
            val newStorage = storage.copy(isFavorite = false)
            if (!newStorage.isWatched && !newStorage.isWatchlist) {
                keysToRemove.add(movieId)
            } else {
                moviesToUpdate[movieId] = newStorage
            }
        }
        
        keysToRemove.forEach { storedMovies.remove(it) }
        moviesToUpdate.forEach { (movieId, storage) -> storedMovies[movieId] = storage }
        
        saveStoredMovies(storedMovies)
    }
    
    fun clearWatched() {
        val storedMovies = getStoredMovies().toMutableMap()
        val keysToRemove = mutableListOf<Int>()
        val moviesToUpdate = mutableMapOf<Int, MovieStorage>()
        
        for ((movieId, storage) in storedMovies) {
            val newStorage = storage.copy(isWatched = false)
            if (!newStorage.isFavorite && !newStorage.isWatchlist) {
                keysToRemove.add(movieId)
            } else {
                moviesToUpdate[movieId] = newStorage
            }
        }
        
        keysToRemove.forEach { storedMovies.remove(it) }
        moviesToUpdate.forEach { (movieId, storage) -> storedMovies[movieId] = storage }
        
        saveStoredMovies(storedMovies)
    }
    
    fun clearWatchlist() {
        val storedMovies = getStoredMovies().toMutableMap()
        val keysToRemove = mutableListOf<Int>()
        val moviesToUpdate = mutableMapOf<Int, MovieStorage>()
        
        for ((movieId, storage) in storedMovies) {
            val newStorage = storage.copy(isWatchlist = false)
            if (!newStorage.isFavorite && !newStorage.isWatched) {
                keysToRemove.add(movieId)
            } else {
                moviesToUpdate[movieId] = newStorage
            }
        }
        
        keysToRemove.forEach { storedMovies.remove(it) }
        moviesToUpdate.forEach { (movieId, storage) -> storedMovies[movieId] = storage }
        
        saveStoredMovies(storedMovies)
    }
}