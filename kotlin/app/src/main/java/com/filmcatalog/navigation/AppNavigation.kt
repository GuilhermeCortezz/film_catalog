package com.filmcatalog.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.filmcatalog.data.model.Movie
import com.filmcatalog.navigation.MovieCache
import com.filmcatalog.ui.screens.MainScreen
import com.filmcatalog.ui.screens.MovieDetailsScreen
import com.filmcatalog.ui.screens.SectionScreen
import com.filmcatalog.ui.screens.CategoriesScreen
import com.filmcatalog.ui.screens.MovieSection
import com.google.gson.Gson

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Main screen with bottom navigation
        composable("main") {
            MainScreen(
                onMovieClick = { movie ->
                    MovieCache.putMovie(movie)
                    navController.navigate("movie_details/${movie.id}")
                },
                onSectionClick = { section ->
                    navController.navigate("section/${section.endpoint}/${section.title}")
                },
                onCategoriesClick = {
                    navController.navigate("categories")
                }
            )
        }
        
        // Movie details
        composable(
            "movie_details/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
            val movie = MovieCache.getMovie(movieId)
            
            if (movie != null) {
                MovieDetailsScreen(
                    movie = movie,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        
        // Section screen
        composable(
            "section/{endpoint}/{title}",
            arguments = listOf(
                navArgument("endpoint") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val endpoint = backStackEntry.arguments?.getString("endpoint") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            
            val section = when (endpoint) {
                "popular" -> MovieSection.POPULAR
                "top_rated" -> MovieSection.TOP_RATED
                "now_playing" -> MovieSection.NOW_PLAYING
                "upcoming" -> MovieSection.UPCOMING
                else -> MovieSection.POPULAR
            }
            
            SectionScreen(
                section = section,
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie ->
                    MovieCache.putMovie(movie)
                    navController.navigate("movie_details/${movie.id}")
                }
            )
        }
        
        // Categories screen
        composable("categories") {
            CategoriesScreen(
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie ->
                    MovieCache.putMovie(movie)
                    navController.navigate("movie_details/${movie.id}")
                }
            )
        }
        
        // Categories with specific genre
        composable(
            "categories/{genreId}/{genreName}",
            arguments = listOf(
                navArgument("genreId") { type = NavType.IntType },
                navArgument("genreName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getInt("genreId")
            val genreName = backStackEntry.arguments?.getString("genreName")
            
            CategoriesScreen(
                genreId = genreId,
                genreName = genreName,
                onBackClick = { navController.popBackStack() },
                onMovieClick = { movie ->
                    MovieCache.putMovie(movie)
                    navController.navigate("movie_details/${movie.id}")
                }
            )
        }
    }
}