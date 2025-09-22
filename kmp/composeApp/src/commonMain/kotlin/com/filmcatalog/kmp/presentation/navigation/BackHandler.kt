package com.filmcatalog.kmp.presentation.navigation

import androidx.compose.runtime.Composable

@Composable
expect fun HandleBackPressed(onBackPressed: () -> Unit)