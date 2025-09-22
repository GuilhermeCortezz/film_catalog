package com.filmcatalog.kmp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.filmcatalog.kmp.data.calendar.CalendarManager

@Composable
actual fun rememberCalendarManager(): CalendarManager {
    return remember { CalendarManager() }
}