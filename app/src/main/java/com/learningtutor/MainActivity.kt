package com.learningtutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.learningtutor.ui.screens.LearningApp
import com.learningtutor.ui.theme.LearningTutorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var isInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LearningTutorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isInitialized) {
                        LearningApp()
                    } else {
                        // Показываем экран загрузки
                        LoadingScreen {
                            isInitialized = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(onInitialized: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()

        // Симулируем загрузку
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000) // 2 секунды загрузки
            onInitialized()
        }
    }
}