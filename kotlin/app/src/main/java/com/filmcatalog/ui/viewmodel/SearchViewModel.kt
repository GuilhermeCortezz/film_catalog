package com.filmcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.service.TMDBService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val movies: List<Movie> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, error = null)
        
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.trim().length >= 2) {
            // Debounce search
            searchJob = viewModelScope.launch {
                delay(500)
                searchMovies()
            }
        } else if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(movies = emptyList())
        }
    }
    
    fun searchMovies() {
        val query = _uiState.value.searchQuery.trim()
        if (query.length < 2) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val movies = TMDBService.searchMovies(query)
                _uiState.value = _uiState.value.copy(
                    movies = movies,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro desconhecido"
                )
            }
        }
    }
    
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = SearchUiState()
    }
}