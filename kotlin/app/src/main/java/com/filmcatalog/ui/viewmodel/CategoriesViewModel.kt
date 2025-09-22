package com.filmcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmcatalog.data.model.Genre
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.service.TMDBService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val genres: List<Genre> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val selectedGenreId: Int? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null
)

class CategoriesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()
    
    fun loadGenres() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val genres = TMDBService.getGenres()
                _uiState.value = _uiState.value.copy(
                    genres = genres,
                    isLoading = false
                )
                
                // Load popular movies by default
                loadMoviesByGenre(page = 1, reset = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }
    
    fun selectGenre(genreId: Int?) {
        _uiState.value = _uiState.value.copy(
            selectedGenreId = genreId,
            movies = emptyList(),
            currentPage = 1,
            hasMore = true
        )
        loadMoviesByGenre(page = 1, reset = true)
    }
    
    fun loadMoviesByGenre(page: Int = 1, reset: Boolean = false) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }
            
            try {
                val response = if (_uiState.value.selectedGenreId != null) {
                    TMDBService.getMoviesByGenrePaginated(_uiState.value.selectedGenreId!!, page)
                } else {
                    TMDBService.getPopularMoviesPaginated(page)
                }
                
                val newMovies = if (reset || page == 1) {
                    response.results
                } else {
                    _uiState.value.movies + response.results
                }
                
                _uiState.value = _uiState.value.copy(
                    movies = newMovies,
                    currentPage = response.page,
                    totalPages = response.total_pages,
                    hasMore = response.page < response.total_pages,
                    isLoading = false,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }
    
    fun loadMoreMovies() {
        if (!_uiState.value.isLoadingMore && _uiState.value.hasMore) {
            loadMoviesByGenre(_uiState.value.currentPage + 1, false)
        }
    }
    
    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadMoviesByGenre(page = 1, reset = true)
        _uiState.value = _uiState.value.copy(isRefreshing = false)
    }
}