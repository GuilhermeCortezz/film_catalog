package com.filmcatalog.kmp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.filmcatalog.kmp.data.calendar.CalendarManager

@Composable
actual fun rememberCalendarManager(): CalendarManager {
    val context = LocalContext.current
    return remember { CalendarManager(context) }
}