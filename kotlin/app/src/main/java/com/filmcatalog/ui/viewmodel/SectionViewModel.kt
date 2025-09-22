package com.filmcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.service.TMDBService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SectionUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true
)

class SectionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SectionUiState())
    val uiState: StateFlow<SectionUiState> = _uiState.asStateFlow()
    
    private var currentEndpoint = ""
    
    fun loadMovies(endpoint: String) {
        currentEndpoint = endpoint
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                currentPage = 1,
                movies = emptyList()
            )
            
            try {
                val movies = getMoviesByEndpoint(endpoint, 1)
                _uiState.value = _uiState.value.copy(
                    movies = movies,
                    isLoading = false,
                    canLoadMore = movies.isNotEmpty()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }
    
    fun loadMoreMovies() {
        if (_uiState.value.isLoadingMore || !_uiState.value.canLoadMore) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            try {
                val nextPage = _uiState.value.currentPage + 1
                val newMovies = getMoviesByEndpoint(currentEndpoint, nextPage)
                
                _uiState.value = _uiState.value.copy(
                    movies = _uiState.value.movies + newMovies,
                    isLoadingMore = false,
                    currentPage = nextPage,
                    canLoadMore = newMovies.isNotEmpty()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = e.message ?: "Erro ao carregar mais filmes"
                )
            }
        }
    }
    
    private suspend fun getMoviesByEndpoint(endpoint: String, page: Int): List<Movie> {
        return when (endpoint) {
            "popular" -> TMDBService.getPopularMovies(page)
            "top_rated" -> TMDBService.getTopRatedMovies(page)
            "now_playing" -> TMDBService.getNowPlayingMovies(page)
            "upcoming" -> TMDBService.getUpcomingMovies(page)
            else -> emptyList()
        }
    }
}