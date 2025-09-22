package com.filmcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.service.TMDBService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val popularMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val nowPlayingMovies: List<Movie> = emptyList(),
    val upcomingMovies: List<Movie> = emptyList(),
    val isLoadingPopular: Boolean = false,
    val isLoadingTopRated: Boolean = false,
    val isLoadingNowPlaying: Boolean = false,
    val isLoadingUpcoming: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadMovies() {
        loadPopularMovies()
        loadTopRatedMovies()
        loadNowPlayingMovies()
        loadUpcomingMovies()
    }
    
    private fun loadPopularMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPopular = true)
            try {
                val movies = TMDBService.getPopularMovies()
                _uiState.value = _uiState.value.copy(
                    popularMovies = movies,
                    isLoadingPopular = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPopular = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun loadTopRatedMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingTopRated = true)
            try {
                val movies = TMDBService.getTopRatedMovies()
                _uiState.value = _uiState.value.copy(
                    topRatedMovies = movies,
                    isLoadingTopRated = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingTopRated = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun loadNowPlayingMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingNowPlaying = true)
            try {
                val movies = TMDBService.getNowPlayingMovies()
                _uiState.value = _uiState.value.copy(
                    nowPlayingMovies = movies,
                    isLoadingNowPlaying = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingNowPlaying = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun loadUpcomingMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUpcoming = true)
            try {
                val movies = TMDBService.getUpcomingMovies()
                _uiState.value = _uiState.value.copy(
                    upcomingMovies = movies,
                    isLoadingUpcoming = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingUpcoming = false,
                    error = e.message
                )
            }
        }
    }
}