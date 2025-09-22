package com.filmcatalog.kmp.presentation.navigation

import androidx.compose.runtime.*
import com.filmcatalog.kmp.data.model.Movie
import com.filmcatalog.kmp.data.storage.MovieStatus
import com.filmcatalog.kmp.presentation.screens.*

enum class MainTab {
    HOME, SEARCH, PROFILE
}

sealed class Screen {
    data class Main(val activeTab: MainTab = MainTab.HOME) : Screen()
    data class MovieDetails(val movie: Movie, val fromTab: MainTab = MainTab.HOME) : Screen()
    data class Section(val title: String, val endpoint: String) : Screen()
    object Categories : Screen()
    data class MovieList(val status: MovieStatus, val fromTab: MainTab = MainTab.PROFILE) : Screen()
}

@Composable
fun AppNavigation() {
    var navigationStack by remember { mutableStateOf<List<Screen>>(listOf(Screen.Main())) }
    var searchQuery by remember { mutableStateOf("") }
    val currentScreen = navigationStack.last()

    fun navigateTo(screen: Screen) {
        navigationStack = navigationStack + screen
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack = navigationStack.dropLast(1)
        }
    }

    // Handle back button for Android
    HandleBackPressed {
        navigateBack()
    }

    when (val screen = currentScreen) {
        is Screen.Main -> {
            MainScreen(
                initialTab = screen.activeTab,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onMovieClick = { movie ->
                    navigateTo(Screen.MovieDetails(movie, screen.activeTab))
                },
                onSectionClick = { endpoint, title ->
                    navigateTo(Screen.Section(title, endpoint))
                },
                onCategoriesClick = {
                    navigateTo(Screen.Categories)
                },
                onProfileSectionClick = { status ->
                    navigateTo(Screen.MovieList(status, MainTab.PROFILE))
                },
                onTabChanged = { newTab ->
                    // Update the current screen's tab without adding to navigation stack
                    navigationStack = navigationStack.dropLast(1) + Screen.Main(newTab)
                }
            )
        }
        
        is Screen.MovieDetails -> {
            MovieDetailsScreen(
                movie = screen.movie,
                onBackClick = { 
                    // Navigate back to Main with the correct tab
                    navigateBack()
                }
            )
        }
        
        is Screen.Section -> {
            SectionScreen(
                title = screen.title,
                endpoint = screen.endpoint,
                onBackClick = { navigateBack() },
                onMovieClick = { movie ->
                    navigateTo(Screen.MovieDetails(movie))
                }
            )
        }
        
        is Screen.Categories -> {
            CategoriesScreen(
                onBackClick = { navigateBack() },
                onMovieClick = { movie: Movie ->
                    navigateTo(Screen.MovieDetails(movie, MainTab.HOME))
                }
            )
        }
        
        is Screen.MovieList -> {
            MovieListScreen(
                status = screen.status,
                onBackClick = { navigateBack() },
                onMovieClick = { movie ->
                    navigateTo(Screen.MovieDetails(movie, screen.fromTab))
                }
            )
        }
    }
}