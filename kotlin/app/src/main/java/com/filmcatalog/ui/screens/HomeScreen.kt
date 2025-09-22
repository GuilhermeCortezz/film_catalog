package com.filmcatalog.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.filmcatalog.R
import com.filmcatalog.data.model.Movie
import com.filmcatalog.navigation.MovieCache
import com.filmcatalog.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Movie) -> Unit = {},
    onSectionClick: (MovieSection) -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadMovies()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        // Custom Top Bar
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Popular Movies Section
            item {
                MovieSection(
                    title = "Populares",
                    movies = uiState.popularMovies,
                    isLoading = uiState.isLoadingPopular,
                    onMovieClick = onMovieClick,
                    onSeeAllClick = { 
                        onSectionClick(com.filmcatalog.ui.screens.MovieSection.POPULAR) 
                    }
                )
            }
            
            // Top Rated Movies Section
            item {
                MovieSection(
                    title = "Mais Bem Avaliados",
                    movies = uiState.topRatedMovies,
                    isLoading = uiState.isLoadingTopRated,
                    onMovieClick = onMovieClick,
                    onSeeAllClick = { 
                        onSectionClick(com.filmcatalog.ui.screens.MovieSection.TOP_RATED) 
                    }
                )
            }
            
            // Now Playing Movies Section
            item {
                MovieSection(
                    title = "Em Cartaz",
                    movies = uiState.nowPlayingMovies,
                    isLoading = uiState.isLoadingNowPlaying,
                    onMovieClick = onMovieClick,
                    onSeeAllClick = { 
                        onSectionClick(com.filmcatalog.ui.screens.MovieSection.NOW_PLAYING) 
                    }
                )
            }
            
            // Upcoming Movies Section
            item {
                MovieSection(
                    title = "Próximos Lançamentos",
                    movies = uiState.upcomingMovies,
                    isLoading = uiState.isLoadingUpcoming,
                    onMovieClick = onMovieClick,
                    onSeeAllClick = { 
                        onSectionClick(com.filmcatalog.ui.screens.MovieSection.UPCOMING) 
                    }
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

@Composable
fun MovieSection(
    title: String,
    movies: List<Movie>,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onSeeAllClick: (() -> Unit)? = null
) {
    Column {
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
            
            if (onSeeAllClick != null) {
                TextButton(onClick = onSeeAllClick) {
                    Text(
                        text = "Ver todos",
                        color = Color(0xFFE94560),
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(5) {
                    MovieCardSkeleton()
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(movies) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { 
                            MovieCache.putMovie(movie)
                            onMovieClick(movie) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = movie.posterImageUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_launcher_foreground),
            error = painterResource(R.drawable.ic_launcher_foreground)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(120.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(120.dp)
        ) {
            Text(
                text = "⭐",
                fontSize = 10.sp
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = String.format("%.1f", movie.voteAverage),
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MovieCardSkeleton() {
    Column(
        modifier = Modifier.width(120.dp)
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        )
    }
}