package com.filmcatalog.navigation

import com.filmcatalog.data.model.Movie

object MovieCache {
    private val cache = mutableMapOf<Int, Movie>()
    
    fun cacheMovie(movie: Movie) {
        cache[movie.id] = movie
    }
    
    fun putMovie(movie: Movie) {
        cache[movie.id] = movie
    }
    
    fun getCachedMovie(movieId: Int): Movie? {
        return cache[movieId]
    }
    
    fun getMovie(id: Int): Movie? {
        return cache[id]
    }
    
    fun clearCache() {
        cache.clear()
    }
}