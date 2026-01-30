package com.learningtutor

import android.app.Application
import android.util.Log
import com.learningtutor.core.ml.GenerationManager
import com.learningtutor.data.database.AppDatabase
import com.learningtutor.data.repositories.QuestionRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LearningTutorApplication : Application() {

    @Inject
    lateinit var questionRepository: QuestionRepository

    @Inject
    lateinit var database: AppDatabase

    private lateinit var generationManager: GenerationManager

    override fun onCreate() {
        super.onCreate()

        // Инициализация в фоне
        CoroutineScope(Dispatchers.IO).launch {
            initializeApp()
        }
    }

    private suspend fun initializeApp() {
        try {
            // 1. Инициализируем базу данных
            database.query("SELECT 1", null) // Простой запрос для инициализации

            // 2. Инициализируем начальные данные
            questionRepository.initializeSeedQuestions()

            // 3. Инициализируем генератор заданий
            generationManager = GenerationManager(this, questionRepository)
            generationManager.initialize()

            // 4. Связываем генератор с репозиторием
            questionRepository.setGenerationManager(generationManager)

            Log.d("LearningTutor", "Application initialized successfully")
        } catch (e: Exception) {
            Log.e("LearningTutor", "Failed to initialize application", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        generationManager.cleanup()
    }
}