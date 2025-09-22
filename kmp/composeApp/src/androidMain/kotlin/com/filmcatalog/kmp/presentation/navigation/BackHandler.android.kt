package com.filmcatalog.kmp.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun HandleBackPressed(onBackPressed: () -> Unit) {
    BackHandler(onBack = onBackPressed)
}