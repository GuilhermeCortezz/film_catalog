package com.filmcatalog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.model.MovieSchedule
import com.filmcatalog.data.service.MovieStorageService
import com.filmcatalog.data.service.MovieScheduleService
import com.filmcatalog.navigation.MovieCache
import java.text.SimpleDateFormat
import java.util.*

enum class ProfileTab(val title: String, val icon: ImageVector) {
    FAVORITES("Favoritos", Icons.Default.Favorite),
    WATCHED("Assistidos", Icons.Default.PlayArrow),
    WATCHLIST("Quero ver", Icons.Default.List),
    SCHEDULES("Agenda", Icons.Default.DateRange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onMovieClick: (Movie) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storageService = remember { MovieStorageService(context) }
    val scheduleService = remember { MovieScheduleService(context) }
    
    var selectedTab by remember { mutableStateOf(ProfileTab.FAVORITES) }
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var schedules by remember { mutableStateOf<List<MovieSchedule>>(emptyList()) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    // Load data when tab changes
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            ProfileTab.FAVORITES -> {
                movies = storageService.getFavoriteMovies()
            }
            ProfileTab.WATCHED -> {
                movies = storageService.getWatchedMovies()
            }
            ProfileTab.WATCHLIST -> {
                movies = storageService.getWatchlistMovies()
            }
            ProfileTab.SCHEDULES -> {
                schedules = scheduleService.getAllSchedules()
            }
        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meu Perfil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                if (movies.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpar ${selectedTab.title.lowercase()}",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        
        // Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Tab Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color(0xFFE94560) else Color.Transparent
                                )
                                .clickable { selectedTab = tab }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) Color.White else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
            
            // Content
            if (selectedTab == ProfileTab.SCHEDULES) {
                // Schedule List
                if (schedules.isEmpty()) {
                    // Empty state for schedules
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Nenhum agendamento",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Agende filmes para assistir e veja os lembretes aqui",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(schedules) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                onMovieClick = { movieId ->
                                    // Find movie in cache or create a basic one
                                    val movie = MovieCache.getMovie(movieId) ?: Movie(
                                        id = schedule.movieId,
                                        title = schedule.movieTitle,
                                        overview = "",
                                        posterPath = schedule.moviePoster,
                                        backdropPath = null,
                                        releaseDate = "",
                                        voteAverage = 0.0,
                                        voteCount = 0,
                                        popularity = 0.0,
                                        genreIds = emptyList(),
                                        adult = false,
                                        video = false,
                                        originalLanguage = "",
                                        originalTitle = schedule.movieTitle
                                    )
                                    onMovieClick(movie)
                                }
                            )
                        }
                    }
                }
            } else if (movies.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = selectedTab.icon,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = when (selectedTab) {
                                ProfileTab.FAVORITES -> "Nenhum favorito"
                                ProfileTab.WATCHED -> "Nenhum filme assistido"
                                ProfileTab.WATCHLIST -> "Nenhum quero ver"
                                ProfileTab.SCHEDULES -> "Nenhum agendamento"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = when (selectedTab) {
                                ProfileTab.FAVORITES -> "Adicione filmes aos favoritos\npara vê-los aqui"
                                ProfileTab.WATCHED -> "Marque filmes como assistidos\npara vê-los aqui"
                                ProfileTab.WATCHLIST -> "Marque filmes como quero ver\npara vê-los aqui"
                                ProfileTab.SCHEDULES -> "Agende filmes para assistir\ne veja os lembretes aqui"
                            },
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Movie grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 48.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
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
    
    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(
                    text = "Limpar lista",
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Tem certeza que deseja limpar toda a lista de ${selectedTab.title.lowercase()}?",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Clear the specific list
                        when (selectedTab) {
                            ProfileTab.FAVORITES -> storageService.clearFavorites()
                            ProfileTab.WATCHED -> storageService.clearWatched()
                            ProfileTab.WATCHLIST -> storageService.clearWatchlist()
                            ProfileTab.SCHEDULES -> scheduleService.clearAllSchedules()
                        }
                        
                        // Refresh the data
                        when (selectedTab) {
                            ProfileTab.FAVORITES -> {
                                movies = storageService.getFavoriteMovies()
                            }
                            ProfileTab.WATCHED -> {
                                movies = storageService.getWatchedMovies()
                            }
                            ProfileTab.WATCHLIST -> {
                                movies = storageService.getWatchlistMovies()
                            }
                            ProfileTab.SCHEDULES -> {
                                schedules = scheduleService.getAllSchedules()
                            }
                        }
                        showClearDialog = false
                    }
                ) {
                    Text(
                        text = "Sim",
                        color = Color(0xFFE94560)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color.Gray
                    )
                }
            },
            containerColor = Color(0xFF2A2A2A)
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: MovieSchedule,
    onMovieClick: (Int) -> Unit
) {
    val currentTime = System.currentTimeMillis()
    val scheduleDate = Date(schedule.scheduledDate)
    val isUpcoming = schedule.scheduledDate > currentTime
    val isPast = schedule.scheduledDate < currentTime

    val formatDate = { date: Date ->
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale("pt", "BR"))
        sdf.format(date)
    }

    val formatTime = { date: Date ->
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(date)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClick(schedule.movieId) },
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) Color(0xFF1A1A1A) else Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Movie Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = schedule.movieTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPast) Color.Gray else Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isPast) Color.Gray else Color(0xFFFF6B35),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = "${formatDate(scheduleDate)} às ${formatTime(scheduleDate)}",
                        fontSize = 14.sp,
                        color = if (isPast) Color.Gray else Color.White
                    )
                }
                
                if (!schedule.notes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = schedule.notes,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }
            
            // Status Icon
            Icon(
                imageVector = when {
                    isPast -> Icons.Default.PlayArrow
                    isUpcoming -> Icons.Default.DateRange
                    else -> Icons.Default.DateRange
                },
                contentDescription = null,
                tint = when {
                    isPast -> Color.Gray
                    isUpcoming -> Color(0xFFFFD700)
                    else -> Color(0xFF4CAF50)
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}