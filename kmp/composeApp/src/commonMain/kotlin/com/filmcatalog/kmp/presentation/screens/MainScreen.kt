package com.filmcatalog.kmp.presentation.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.filmcatalog.kmp.data.model.Movie
import com.filmcatalog.kmp.data.storage.MovieStatus
import com.filmcatalog.kmp.presentation.navigation.MainTab

enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val tab: MainTab
) {
    HOME("Início", Icons.Default.Home, "home", MainTab.HOME),
    SEARCH("Buscar", Icons.Default.Search, "search", MainTab.SEARCH),
    PROFILE("Perfil", Icons.Default.Person, "profile", MainTab.PROFILE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialTab: MainTab = MainTab.HOME,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onSectionClick: (String, String) -> Unit = { _, _ -> },
    onCategoriesClick: () -> Unit = {},
    onProfileSectionClick: (MovieStatus) -> Unit = {},
    onTabChanged: (MainTab) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    
    // Update selectedTab when initialTab changes
    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    
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
                        selected = selectedTab == item.tab,
                        onClick = { 
                            selectedTab = item.tab
                            onTabChanged(item.tab)
                        },
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
            MainTab.HOME -> {
                SimpleHomeScreen(
                    onMovieClick = onMovieClick,
                    onSectionClick = onSectionClick,
                    onCategoriesClick = onCategoriesClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            MainTab.SEARCH -> {
                SearchScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onMovieClick = onMovieClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            MainTab.PROFILE -> {
                ProfileScreen(
                    onSectionClick = onProfileSectionClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}