package com.learningtutor.core.ml

import android.content.Context
import androidx.work.*
import com.learningtutor.data.repositories.QuestionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Менеджер фоновой генерации заданий
 */
class GenerationManager(
    private val context: Context,
    private val questionRepository: QuestionRepository
) {

    private val llmGenerator = LLMGenerator(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isInitialized = false

    /**
     * Инициализирует генератор и запускает фоновую генерацию
     */
    suspend fun initialize() {
        scope.launch {
            isInitialized = llmGenerator.initialize()

            if (isInitialized) {
                // Запускаем периодическую генерацию (опционально)
                // scheduleBackgroundGeneration()

                // Генерируем начальный набор вопросов
                generateInitialQuestions()
            }
        }
    }

    /**
     * Генерирует вопрос по требованию
     */
    suspend fun generateOnDemand(
        topic: String,
        difficulty: Double = 2.0
    ): Result<com.learningtutor.core.models.Question> {
        if (!isInitialized) {
            return Result.failure(IllegalStateException("Generator not initialized"))
        }

        return llmGenerator.generateQuestion(topic, difficulty)
    }

    /**
     * Освобождает ресурсы
     */
    fun cleanup() {
        llmGenerator.cleanup()
    }

    private fun scheduleBackgroundGeneration() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val generationWork = PeriodicWorkRequestBuilder<GenerationWorker>(
            4, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "background_generation",
                ExistingPeriodicWorkPolicy.KEEP,
                generationWork
            )
    }

    private suspend fun generateInitialQuestions() {
        val topics = listOf("Дроби", "Уравнения", "Геометрия", "Проценты")
        val difficulties = listOf(1.0, 2.0, 3.0)

        try {
            val questions = llmGenerator.generateBatch(topics, difficulties, 2)

            questions.forEach { question ->
                questionRepository.insertGeneratedQuestion(question)
            }
        } catch (e: Exception) {
            // Игнорируем ошибки при начальной генерации
        }
    }
}

/**
 * Worker для фоновой генерации
 */
class GenerationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Здесь можно добавить логику генерации
            // Пока просто возвращаем успех
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}