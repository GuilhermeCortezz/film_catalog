package com.filmcatalog.kmp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.filmcatalog.kmp.data.model.Movie
import com.filmcatalog.kmp.data.remote.TMDBService
import kotlinx.coroutines.delay

data class Genre(
    val id: Int,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var genres by remember { mutableStateOf<List<Genre>>(emptyList()) }
    var selectedGenre by remember { mutableStateOf<Genre?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }
    
    val tmdbService = remember { TMDBService() }
    val gridState = rememberLazyGridState()

    // Initialize genres and load initial movies
    LaunchedEffect(Unit) {
        suspend fun loadGenres() {
            try {
                val genresResponse = tmdbService.getGenres()
                genres = listOf(Genre(0, "Todos")) + genresResponse.genres.map { 
                    Genre(it.id, it.name) 
                }
            } catch (e: Exception) {
                error = "Erro ao carregar gêneros: ${e.message}"
            }
        }
        
        suspend fun loadMovies(page: Int = 1, reset: Boolean = false) {
            try {
                if (page == 1) {
                    isLoading = true
                    error = null
                } else {
                    isLoadingMore = true
                }

                val response = if (selectedGenre == null || selectedGenre?.id == 0) {
                    tmdbService.getPopularMovies(page)
                } else {
                    tmdbService.discoverMovies(
                        page = page,
                        withGenres = selectedGenre?.id.toString()
                    )
                }

                if (reset || page == 1) {
                    movies = response.results
                } else {
                    movies = movies + response.results
                }

                currentPage = response.page
                totalPages = response.totalPages
                hasMore = response.page < response.totalPages

            } catch (e: Exception) {
                error = "Erro ao carregar filmes: ${e.message}"
            } finally {
                isLoading = false
                isLoadingMore = false
            }
        }
        
        loadGenres()
        loadMovies(1, true)
    }

    // Load movies when genre changes
    LaunchedEffect(selectedGenre) {
        suspend fun loadMovies(page: Int = 1, reset: Boolean = false) {
            try {
                if (page == 1) {
                    isLoading = true
                    error = null
                } else {
                    isLoadingMore = true
                }

                val response = if (selectedGenre == null || selectedGenre?.id == 0) {
                    tmdbService.getPopularMovies(page)
                } else {
                    tmdbService.discoverMovies(
                        page = page,
                        withGenres = selectedGenre?.id.toString()
                    )
                }

                if (reset || page == 1) {
                    movies = response.results
                } else {
                    movies = movies + response.results
                }

                currentPage = response.page
                totalPages = response.totalPages
                hasMore = response.page < response.totalPages

            } catch (e: Exception) {
                error = "Erro ao carregar filmes: ${e.message}"
            } finally {
                isLoading = false
                isLoadingMore = false
            }
        }
        
        loadMovies(1, true)
    }

    // Pagination effect - detect when user scrolls to bottom
    LaunchedEffect(gridState) {
        suspend fun loadMoreMovies(page: Int = 1, reset: Boolean = false) {
            try {
                if (page == 1) {
                    isLoading = true
                    error = null
                } else {
                    isLoadingMore = true
                }

                val response = if (selectedGenre == null || selectedGenre?.id == 0) {
                    tmdbService.getPopularMovies(page)
                } else {
                    tmdbService.discoverMovies(
                        page = page,
                        withGenres = selectedGenre?.id.toString()
                    )
                }

                if (reset || page == 1) {
                    movies = response.results
                } else {
                    movies = movies + response.results
                }

                currentPage = response.page
                totalPages = response.totalPages
                hasMore = response.page < response.totalPages

            } catch (e: Exception) {
                error = "Erro ao carregar filmes: ${e.message}"
            } finally {
                isLoading = false
                isLoadingMore = false
            }
        }
        
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= movies.size - 5 && 
                    !isLoadingMore && 
                    hasMore &&
                    currentPage < totalPages) {
                    loadMoreMovies(currentPage + 1, false)
                }
            }
    }

    fun handleGenreSelect(genre: Genre) {
        selectedGenre = genre
        currentPage = 1
        movies = emptyList()
    }

    // UI Rendering
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Header
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = selectedGenre?.name ?: "Categorias",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (movies.isNotEmpty()) {
                        Text(
                            text = "${movies.size} ${if (movies.size == 1) "filme" else "filmes"}${
                                if (totalPages > 1) " • Página $currentPage de $totalPages" else ""
                            }",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A1A1A)
            )
        )

        // Genre Filter
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(genres) { genre ->
                FilterChip(
                    onClick = { handleGenreSelect(genre) },
                    label = {
                        Text(
                            text = genre.name,
                            fontSize = 14.sp
                        )
                    },
                    selected = selectedGenre?.id == genre.id,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF2A2A2A),
                        labelColor = Color.White,
                        selectedContainerColor = Color(0xFFE50914),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFE50914),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Carregando filmes...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            error != null && movies.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Erro desconhecido",
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                // Trigger reload by changing selectedGenre
                                val currentGenre = selectedGenre
                                selectedGenre = null
                                selectedGenre = currentGenre
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE50914)
                            )
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }
            
            movies.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎬",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum filme encontrado",
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tente selecionar uma categoria diferente",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(movies) { movie ->
                        MovieCategoryCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                    
                    // Loading more indicator
                    if (isLoadingMore) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFE50914),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Carregando mais filmes...",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCategoryCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Movie Poster
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Movie Info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = movie.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", movie.voteAverage),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    if (movie.releaseDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = movie.releaseDate.take(4),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}