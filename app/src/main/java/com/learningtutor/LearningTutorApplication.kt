package com.learningtutor

import android.app.Application
import android.util.Log
import com.learningtutor.core.ml.GenerationManager
import com.learningtutor.data.repositories.QuestionRepository
import com.learningtutor.data.repositories.UserRepository
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
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var generationManager: GenerationManager

    override fun onCreate() {
        super.onCreate()

        // Инициализация в фоне
        CoroutineScope(Dispatchers.IO).launch {
            initializeApp()
        }
    }

    private suspend fun initializeApp() {
        try {
            Log.d("LearningTutor", "Начинаем инициализацию приложения...")

            // 1. Создаем пользователя, если его нет
            userRepository.createUserIfNotExists()
            Log.d("LearningTutor", "Пользователь создан/найден")

            // 2. Инициализируем начальные вопросы
            questionRepository.initializeSeedQuestions()
            Log.d("LearningTutor", "Seed вопросы инициализированы")

            // 3. Инициализируем генератор
            generationManager.initialize()
            Log.d("LearningTutor", "Генератор инициализирован")

            // 4. Пробуем сгенерировать первые вопросы
            generateInitialQuestions()
            Log.d("LearningTutor", "Application initialized successfully")

        } catch (e: Exception) {
            Log.e("LearningTutor", "Failed to initialize application", e)
        }
    }

    private suspend fun generateInitialQuestions() {
        try {
            val topics = questionRepository.getAllTopics()
            if (topics.isEmpty()) return

            // Генерируем по 1 вопросу для каждой темы
            topics.forEach { topic ->
                try {
                    val generated = questionRepository.generateNewQuestion(topic)
                    if (generated != null) {
                        questionRepository.insertGeneratedQuestion(generated)
                        Log.d("LearningTutor", "Сгенерирован вопрос по теме: $topic")
                    }
                } catch (e: Exception) {
                    Log.w("LearningTutor", "Не удалось сгенерировать вопрос для темы $topic: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("LearningTutor", "Ошибка при генерации начальных вопросов", e)
        }
    }

    override fun onTerminate() {
        try {
            generationManager.cleanup()
        } catch (e: Exception) {
            Log.e("LearningTutor", "Ошибка при очистке генератора", e)
        }
        super.onTerminate()
    }
}