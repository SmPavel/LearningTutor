package com.learningtutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.learningtutor.ui.screens.LearningScreen
import com.learningtutor.ui.theme.LearningTutorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LearningTutorTheme {
                LearningScreen(
                    onProgressClick = {
                        // TODO: Навигация на экран прогресса
                    },
                    onTopicsClick = {
                        // TODO: Навигация на экран тем
                    }
                )
            }
        }
    }
}