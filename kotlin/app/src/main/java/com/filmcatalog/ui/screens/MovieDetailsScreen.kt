package com.filmcatalog.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.filmcatalog.R
import com.filmcatalog.data.model.Movie
import com.filmcatalog.data.model.MovieDetails
import com.filmcatalog.data.model.MovieSchedule
import com.filmcatalog.data.service.MovieStorageService
import com.filmcatalog.data.service.MovieScheduleService
import com.filmcatalog.data.service.TMDBService
import com.filmcatalog.ui.components.ScheduleMovieModal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    movie: Movie,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val storageService = remember { MovieStorageService(context) }
    val scheduleService = remember { MovieScheduleService(context) }
    val scope = rememberCoroutineScope()
    
    var isFavorite by remember { mutableStateOf(storageService.isFavorite(movie.id)) }
    var isWatched by remember { mutableStateOf(storageService.isWatched(movie.id)) }
    var isInWatchlist by remember { mutableStateOf(storageService.isInWatchlist(movie.id)) }
    var movieDetails by remember { mutableStateOf<MovieDetails?>(null) }
    var isLoadingDetails by remember { mutableStateOf(true) }
    var movieSchedule by remember { mutableStateOf<MovieSchedule?>(null) }
    var showScheduleModal by remember { mutableStateOf(false) }
    var isScheduling by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Fetch movie details when screen loads
    LaunchedEffect(movie.id) {
        scope.launch {
            try {
                val details = TMDBService.getMovieDetails(movie.id)
                movieDetails = details
                
                // Load existing schedule
                movieSchedule = scheduleService.getMovieSchedule(movie.id)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingDetails = false
            }
        }
    }

    // Helper functions
    fun formatDate(dateString: String): String {
        return try {
            if (dateString.isBlank()) return "Data não informada"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: "Data inválida"
        } catch (e: Exception) {
            "Data inválida"
        }
    }
    
    fun formatCurrency(amount: Long): String {
        return try {
            if (amount <= 0) return "Não informado"
            val formatted = java.text.NumberFormat.getCurrencyInstance(Locale.US).format(amount)
            formatted
        } catch (e: Exception) {
            "Valor inválido"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        // Background Image
        AsyncImage(
            model = movie.backdropImageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_launcher_foreground),
            error = painterResource(R.drawable.ic_launcher_foreground)
        )
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black
                        )
                    )
                )
        )
        
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
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
        
        // Action Buttons Row (Top Right)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .zIndex(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorite Button
            IconButton(
                onClick = {
                    isFavorite = storageService.toggleFavorite(movie)
                },
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
            
            // Watched Button
            IconButton(
                onClick = {
                    isWatched = storageService.toggleWatched(movie)
                },
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Assistido",
                    tint = if (isWatched) Color.Green else Color.White
                )
            }
            
            // Watchlist Button
            IconButton(
                onClick = {
                    isInWatchlist = storageService.toggleWatchlist(movie)
                },
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Lista para Assistir",
                    tint = if (isInWatchlist) Color(0xFFFF6B35) else Color.White
                )
            }
            
            // Schedule/Calendar Button
            IconButton(
                onClick = {
                    showScheduleModal = true
                },
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Agendar",
                    tint = if (movieSchedule != null) Color(0xFFFFD700) else Color.White
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 200.dp)
        ) {
            // Movie Info Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Poster
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
                
                // Movie Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = movie.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = movie.releaseDate.take(4), // Year only
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = String.format("%.1f", movie.voteAverage),
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = " (${movie.voteCount} votos)",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Genres Section
            movieDetails?.let { details ->
                if (details.genres.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Gêneros",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Genres Tags
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(details.genres.filter { it.name.isNotBlank() }) { genre ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFFF6B35),
                                            RoundedCornerShape(15.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = genre.name,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Overview Section
            if (movie.overview.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sinopse",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = movie.overview,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        lineHeight = 24.sp
                    )
                }
            }
            
            // Additional Info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Informações",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow("Data de Lançamento", formatDate(movie.releaseDate))
                InfoRow("Idioma Original", movie.originalLanguage.uppercase())
                
                // Show budget and revenue from movie details if available
                movieDetails?.let { details ->
                    if (details.budget > 0) {
                        InfoRow("Orçamento", formatCurrency(details.budget))
                    }
                    if (details.revenue > 0) {
                        InfoRow("Bilheteria", formatCurrency(details.revenue))
                    }
                } ?: run {
                    // Fallback if details not loaded yet
                    if (isLoadingDetails) {
                        InfoRow("Orçamento", "Carregando...")
                        InfoRow("Bilheteria", "Carregando...")
                    } else {
                        InfoRow("Orçamento", "Não informado")
                        InfoRow("Bilheteria", "Não informado")
                    }
                }
            }
            
            // Production Section
            movieDetails?.let { details ->
                if (details.productionCompanies.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Produção",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = details.productionCompanies
                                .filter { it.name.isNotBlank() }
                                .joinToString(", ") { it.name }
                                .takeIf { it.isNotBlank() } ?: "Informação não disponível",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            } ?: run {
                // Show loading or fallback if details not loaded yet
                if (isLoadingDetails) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Produção",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Carregando informações de produção...",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Produção",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Informações de produção não disponíveis no momento.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            // Bottom padding for safe area
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Schedule Modal
        ScheduleMovieModal(
            visible = showScheduleModal,
            movie = movie,
            existingSchedule = movieSchedule,
            onDismiss = { showScheduleModal = false },
            onSchedule = { date, notes, addToCalendar ->
                scope.launch {
                    try {
                        isScheduling = true
                        val schedule = scheduleService.scheduleMovie(movie, date, notes, addToCalendar)
                        movieSchedule = schedule
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isScheduling = false
                    }
                }
            },
            onUpdate = { scheduleId, date, notes ->
                scope.launch {
                    try {
                        isScheduling = true
                        val updatedSchedule = scheduleService.updateSchedule(scheduleId, date, notes)
                        movieSchedule = updatedSchedule
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isScheduling = false
                    }
                }
            },
            onRemove = { scheduleId ->
                scope.launch {
                    try {
                        isScheduling = true
                        scheduleService.removeSchedule(scheduleId)
                        movieSchedule = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isScheduling = false
                    }
                }
            },
            loading = isScheduling
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
    
    // Divider line
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.Gray.copy(alpha = 0.2f))
    )
}