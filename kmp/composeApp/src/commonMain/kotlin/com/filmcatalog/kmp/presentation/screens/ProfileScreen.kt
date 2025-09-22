package com.filmcatalog.kmp.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filmcatalog.kmp.data.storage.MovieStatus
import com.filmcatalog.kmp.data.storage.MovieStorageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSectionClick: (MovieStatus) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val storageService = remember { MovieStorageManager.instance }
    
    var favoritesCount by remember { mutableIntStateOf(0) }
    var watchedCount by remember { mutableIntStateOf(0) }
    var watchlistCount by remember { mutableIntStateOf(0) }
    var scheduledCount by remember { mutableIntStateOf(0) }
    
    // Load counts
    LaunchedEffect(Unit) {
        favoritesCount = storageService.getFavoritesCount()
        watchedCount = storageService.getWatchedCount()
        watchlistCount = storageService.getWatchlistCount()
        scheduledCount = storageService.getScheduledCount()
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(0.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(
                    text = "Perfil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // User Profile Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE94560)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Usuário do Cinema",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Amante de filmes desde sempre",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            number = favoritesCount.toString(),
                            label = "Favoritos"
                        )
                        StatItem(
                            number = watchedCount.toString(),
                            label = "Assistidos"
                        )
                        StatItem(
                            number = watchlistCount.toString(),
                            label = "Quero Ver"
                        )
                    }
                }
            }
        }
        
        // Menu Options
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Minhas Listas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Favorite Movies
                MenuOptionCard(
                    icon = Icons.Default.Favorite,
                    title = "Filmes Favoritos",
                    subtitle = "Seus filmes preferidos",
                    count = favoritesCount.toString(),
                    iconColor = Color(0xFFE91E63),
                    onClick = { onSectionClick(MovieStatus.FAVORITE) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Watched Movies
                MenuOptionCard(
                    icon = Icons.Default.PlayArrow,
                    title = "Filmes Assistidos",
                    subtitle = "Histórico de visualizações",
                    count = watchedCount.toString(),
                    iconColor = Color(0xFF4CAF50),
                    onClick = { onSectionClick(MovieStatus.WATCHED) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Watchlist
                MenuOptionCard(
                    icon = Icons.Default.List,
                    title = "Lista de Desejos",
                    subtitle = "Filmes para assistir",
                    count = watchlistCount.toString(),
                    iconColor = Color(0xFF2196F3),
                    onClick = { onSectionClick(MovieStatus.WATCHLIST) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Schedule
                MenuOptionCard(
                    icon = Icons.Default.DateRange,
                    title = "Agenda de Filmes",
                    subtitle = "Filmes agendados para assistir",
                    count = scheduledCount.toString(),
                    iconColor = Color(0xFFE94560),
                    onClick = {
                        onSectionClick(MovieStatus.SCHEDULED)
                    }
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatItem(
    number: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE94560)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun MenuOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: String,
    iconColor: Color,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
            
            // Count badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = count,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

data class MenuItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val count: String? = null,
    val iconColor: Color
)

@Composable
private fun MenuOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: String? = null,
    iconColor: Color,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
            
            // Count badge (if provided)
            count?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A2A2A))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}