package com.filmcatalog.kmp.presentation.navigation

import androidx.compose.runtime.Composable

@Composable
actual fun HandleBackPressed(onBackPressed: () -> Unit) {
    // No-op for iOS, handled by system
}