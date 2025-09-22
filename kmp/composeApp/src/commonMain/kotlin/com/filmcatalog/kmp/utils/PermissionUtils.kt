package com.filmcatalog.kmp.utils

import androidx.compose.runtime.Composable

@Composable
expect fun RequestCalendarPermission(
    onPermissionResult: (Boolean) -> Unit
)