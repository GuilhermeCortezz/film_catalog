package com.filmcatalog.kmp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.filmcatalog.kmp.data.model.Movie
import com.filmcatalog.kmp.data.storage.MovieStatus
import com.filmcatalog.kmp.data.storage.MovieStorageManager
import com.filmcatalog.kmp.presentation.components.ScheduleMovieModal
import com.filmcatalog.kmp.utils.rememberCalendarManager
import com.filmcatalog.kmp.utils.RequestCalendarPermission
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    movie: Movie,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val storageService = remember { MovieStorageManager.instance }
    val calendarManager = rememberCalendarManager()
    val scope = rememberCoroutineScope()
    
    var isFavorite by remember { mutableStateOf(false) }
    var isWatched by remember { mutableStateOf(false) }
    var isWatchlist by remember { mutableStateOf(false) }
    var isScheduled by remember { mutableStateOf(false) }
    var scheduledDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var showScheduleModal by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Load initial status
    LaunchedEffect(movie.id) {
        isFavorite = storageService.isInFavorites(movie.id)
        isWatched = storageService.isWatched(movie.id)
        isWatchlist = storageService.isInWatchlist(movie.id)
        isScheduled = storageService.isScheduled(movie.id)
        
        // Load scheduled date/time if exists
        if (isScheduled) {
            val dateTimeMillis = storageService.getScheduledDateTime(movie.id)
            if (dateTimeMillis != null) {
                scheduledDateTime = Instant.fromEpochMilliseconds(dateTimeMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Movie Poster and Basic Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                // Backdrop Image
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w1280${movie.backdropPath}",
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black
                                ),
                                startY = 0f,
                                endY = 1000f
                            )
                        )
                )
                
                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .zIndex(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                
                // Movie Info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Poster
                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                            contentDescription = movie.title,
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Movie Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = movie.title,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                            
                            if (movie.releaseDate.isNotEmpty()) {
                                Text(
                                    text = movie.releaseDate.take(4),
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "%.1f".format(movie.voteAverage),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Text(
                                    text = "(${movie.voteCount} votos)",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Favorite Button
                    ActionButton(
                        icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        label = "Favoritar",
                        isActive = isFavorite,
                        activeColor = Color(0xFFE91E63),
                        onClick = {
                            scope.launch {
                                if (isFavorite) {
                                    storageService.removeFromFavorites(movie)
                                    isFavorite = false
                                } else {
                                    storageService.addToFavorites(movie)
                                    isFavorite = true
                                }
                            }
                        }
                    )
                    
                    // Watched Button
                    ActionButton(
                        icon = if (isWatched) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow,
                        label = "Assistido",
                        isActive = isWatched,
                        activeColor = Color(0xFF4CAF50),
                        onClick = {
                            scope.launch {
                                if (isWatched) {
                                    storageService.removeFromWatched(movie)
                                    isWatched = false
                                } else {
                                    storageService.addToWatched(movie)
                                    isWatched = true
                                }
                            }
                        }
                    )
                    
                    // Watchlist Button
                    ActionButton(
                        icon = if (isWatchlist) Icons.Filled.List else Icons.Outlined.List,
                        label = "Quero Ver",
                        isActive = isWatchlist,
                        activeColor = Color(0xFF2196F3),
                        onClick = {
                            scope.launch {
                                if (isWatchlist) {
                                    storageService.removeFromWatchlist(movie)
                                    isWatchlist = false
                                } else {
                                    storageService.addToWatchlist(movie)
                                    isWatchlist = true
                                }
                            }
                        }
                    )
                    
                    // Schedule Button
                    ActionButton(
                        icon = if (isScheduled) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                        label = "Agendar",
                        isActive = isScheduled,
                        activeColor = Color(0xFFE94560),
                        onClick = {
                            if (isScheduled) {
                                // Open in edit mode
                                showScheduleModal = true
                            } else {
                                // Add to scheduled
                                scope.launch {
                                    if (calendarManager.hasCalendarPermission()) {
                                        showScheduleModal = true
                                    } else {
                                        showPermissionDialog = true
                                    }
                                }
                            }
                        }
                    )
                }
            }
            
            // Movie Overview
            if (movie.overview.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Sinopse",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Text(
                            text = movie.overview,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // Additional Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Informações",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    InfoRow("Título Original", movie.originalTitle)
                    InfoRow("Idioma", movie.originalLanguage.uppercase())
                    InfoRow("Popularidade", "%.1f".format(movie.popularity))
                    if (movie.releaseDate.isNotEmpty()) {
                        InfoRow("Data de Lançamento", movie.releaseDate)
                    }
                }
            }
            
            // Bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Schedule Modal
    ScheduleMovieModal(
        movie = movie,
        isVisible = showScheduleModal,
        onDismiss = { 
            showScheduleModal = false 
            scheduledDateTime = null // Reset when closing
        },
        onSchedule = { dateTime ->
            scope.launch {
                try {
                    val success = calendarManager.scheduleMovie(movie, dateTime)
                    if (success) {
                        // Save to app storage
                        val dateTimeMillis = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
                        storageService.scheduleMovie(movie, dateTimeMillis)
                        isScheduled = true
                        scheduledDateTime = dateTime
                        println("Movie scheduled successfully!")
                    } else {
                        println("Failed to schedule movie")
                    }
                } catch (e: Exception) {
                    println("Error scheduling movie: ${e.message}")
                } finally {
                    showScheduleModal = false
                }
            }
        },
        onDelete = if (isScheduled) {
            {
                scope.launch {
                    storageService.removeFromScheduled(movie)
                    isScheduled = false
                    scheduledDateTime = null
                    showScheduleModal = false
                }
            }
        } else null,
        initialDateTime = scheduledDateTime,
        isEditMode = isScheduled
    )
    
    // Permission Request
    if (showPermissionDialog) {
        RequestCalendarPermission { granted ->
            showPermissionDialog = false
            if (granted) {
                showScheduleModal = true
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) activeColor else Color.Gray,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}