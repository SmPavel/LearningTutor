package com.learningtutor.core.ml

import android.content.Context
import com.google.gson.Gson
import com.learningtutor.core.models.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Генератор заданий - обертка вокруг SimpleLLMGenerator
 */
class LLMGenerator(private val context: Context) {

    private val simpleGenerator = SimpleLLMGenerator(context)
    private val gson = Gson()
    private var isInitialized = false

    /**
     * Инициализирует генератор
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        isInitialized = simpleGenerator.initialize()
        isInitialized
    }

    /**
     * Генерирует задание по теме
     */
    suspend fun generateQuestion(
        topic: String,
        difficulty: Double,
        subject: String = "math"
    ): Result<Question> = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            return@withContext Result.failure(IllegalStateException("Generator not initialized"))
        }

        simpleGenerator.generateQuestion(topic, difficulty, subject)
    }

    /**
     * Генерирует несколько заданий заранее
     */
    suspend fun generateBatch(
        topics: List<String>,
        difficulties: List<Double>,
        countPerTopic: Int = 3
    ): List<Question> = withContext(Dispatchers.IO) {
        val questions = mutableListOf<Question>()

        for (topic in topics) {
            for (i in 0 until countPerTopic) {
                try {
                    val difficulty = difficulties.getOrNull(i) ?: 2.0
                    val question = simpleGenerator.generateQuestion(topic, difficulty).getOrNull()
                    question?.let { questions.add(it) }
                } catch (e: Exception) {
                    // Пропускаем ошибки
                }
            }
        }

        questions
    }

    /**
     * Освобождает ресурсы
     */
    fun cleanup() {
        simpleGenerator.cleanup()
        isInitialized = false
    }
}