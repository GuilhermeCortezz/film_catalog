package com.filmcatalog.kmp

import androidx.compose.runtime.*
import com.filmcatalog.kmp.presentation.navigation.AppNavigation
import com.filmcatalog.kmp.presentation.theme.FilmCatalogTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    FilmCatalogTheme {
        AppNavigation()
    }
}