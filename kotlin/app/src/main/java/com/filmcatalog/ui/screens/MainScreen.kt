package com.filmcatalog.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.filmcatalog.data.model.Movie

enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Início", Icons.Default.Home, "home"),
    SEARCH("Buscar", Icons.Default.Search, "search"),
    PROFILE("Perfil", Icons.Default.Person, "profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onMovieClick: (Movie) -> Unit = {},
    onSectionClick: (MovieSection) -> Unit = {},
    onCategoriesClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(BottomNavItem.HOME) }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            ) {
                BottomNavItem.values().forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedTab == item,
                        onClick = { selectedTab = item },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE94560),
                            selectedTextColor = Color(0xFFE94560),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE94560).copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            BottomNavItem.HOME -> {
                HomeScreen(
                    onMovieClick = onMovieClick,
                    onSectionClick = onSectionClick,
                    onCategoriesClick = onCategoriesClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            BottomNavItem.SEARCH -> {
                SearchScreen(
                    onMovieClick = onMovieClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            BottomNavItem.PROFILE -> {
                ProfileScreen(
                    onMovieClick = onMovieClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}