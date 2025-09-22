package com.filmcatalog.kmp.utils

import androidx.compose.runtime.Composable

@Composable
actual fun RequestCalendarPermission(
    onPermissionResult: (Boolean) -> Unit
) {
    // iOS permissions are handled differently
    onPermissionResult(false)
}