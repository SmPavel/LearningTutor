package com.learningtutor

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*

@HiltAndroidApp
class LearningTutorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("LearningTutor", "=== APPLICATION START ===")

        // Инициализация в фоне с правильным диспатчером
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            initializeApp()
        }
    }

    private suspend fun initializeApp() {
        try {
            Log.d("LearningTutor", "Начинаем инициализацию приложения...")

            // Даем время на инициализацию Hilt
            delay(100)

            Log.d("LearningTutor", "Application initialized successfully")
        } catch (e: Exception) {
            Log.e("LearningTutor", "Failed to initialize application", e)
        }
    }
}