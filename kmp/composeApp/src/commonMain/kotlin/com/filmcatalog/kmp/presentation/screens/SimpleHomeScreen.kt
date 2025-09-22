package com.filmcatalog.kmp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filmcatalog.kmp.data.model.Movie
import com.filmcatalog.kmp.data.remote.TMDBService
import com.filmcatalog.kmp.presentation.components.MovieCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(
    onMovieClick: (Movie) -> Unit = {},
    onSectionClick: (String, String) -> Unit = { _, _ -> },
    onCategoriesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var popularMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var topRatedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var nowPlayingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var upcomingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    
    var isLoadingPopular by remember { mutableStateOf(true) }
    var isLoadingTopRated by remember { mutableStateOf(true) }
    var isLoadingNowPlaying by remember { mutableStateOf(true) }
    var isLoadingUpcoming by remember { mutableStateOf(true) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val tmdbService = remember { TMDBService() }
    
    LaunchedEffect(Unit) {
        try {
            
            // Load Popular Movies
            val popularResponse = tmdbService.getPopularMovies()
            popularMovies = popularResponse.results
            isLoadingPopular = false
            
            // Load Top Rated Movies
            val topRatedResponse = tmdbService.getTopRatedMovies()
            topRatedMovies = topRatedResponse.results
            isLoadingTopRated = false
            
            // Load Now Playing Movies
            val nowPlayingResponse = tmdbService.getNowPlayingMovies()
            nowPlayingMovies = nowPlayingResponse.results
            isLoadingNowPlaying = false
            
            // Load Upcoming Movies
            val upcomingResponse = tmdbService.getUpcomingMovies()
            upcomingMovies = upcomingResponse.results
            isLoadingUpcoming = false
            
        } catch (e: Exception) {
            errorMessage = "Erro ao carregar filmes: ${e.message}"
            isLoadingPopular = false
            isLoadingTopRated = false
            isLoadingNowPlaying = false
            isLoadingUpcoming = false
            println("DEBUG HomeScreen - Error: ${e.message}")
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.Black)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Catálogo de Filmes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        // Content
        if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Popular Movies Section
                item {
                    MovieSection(
                        title = "Populares",
                        movies = popularMovies,
                        isLoading = isLoadingPopular,
                        onMovieClick = onMovieClick,
                        onSeeAllClick = { onSectionClick("popular", "Populares") }
                    )
                }
                
                // Top Rated Movies Section
                item {
                    MovieSection(
                        title = "Mais Bem Avaliados",
                        movies = topRatedMovies,
                        isLoading = isLoadingTopRated,
                        onMovieClick = onMovieClick,
                        onSeeAllClick = { onSectionClick("top_rated", "Mais Bem Avaliados") }
                    )
                }
                
                // Now Playing Movies Section
                item {
                    MovieSection(
                        title = "Em Cartaz",
                        movies = nowPlayingMovies,
                        isLoading = isLoadingNowPlaying,
                        onMovieClick = onMovieClick,
                        onSeeAllClick = { onSectionClick("now_playing", "Em Cartaz") }
                    )
                }
                
                // Upcoming Movies Section
                item {
                    MovieSection(
                        title = "Próximos Lançamentos",
                        movies = upcomingMovies,
                        isLoading = isLoadingUpcoming,
                        onMovieClick = onMovieClick,
                        onSeeAllClick = { onSectionClick("upcoming", "Próximos Lançamentos") }
                    )
                }
                
                // Explore Categories Button
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onCategoriesClick() },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE94560)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Explorar Categorias",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieSection(
    title: String,
    movies: List<Movie>,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            TextButton(
                onClick = onSeeAllClick
            ) {
                Text(
                    text = "Ver todos",
                    color = Color(0xFFE94560),
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Movies List
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFE94560)
                )
            }
        } else if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum filme encontrado",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(movies.take(10)) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie) }
                    )
                }
            }
        }
    }
}