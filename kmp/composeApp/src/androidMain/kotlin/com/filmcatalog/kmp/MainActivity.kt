package com.filmcatalog.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.filmcatalog.kmp.data.storage.MovieStorageManager
import com.russhwolf.settings.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize storage with platform-specific settings
        val settings = Settings()
        MovieStorageManager.initialize(settings)

        setContent {
            App()
        }
    }
}