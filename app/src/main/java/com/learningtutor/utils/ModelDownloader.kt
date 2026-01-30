package com.learningtutor.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ModelManager {

    private const val MODEL_NAME = "simple_generator"

    /**
     * Проверяет, доступна ли модель (всегда true для простого генератора)
     */
    suspend fun isModelAvailable(context: Context): Boolean = withContext(Dispatchers.IO) {
        // Для SimpleLLMGenerator модель всегда доступна
        true
    }

    /**
     * Получает путь к модели (пустая строка для простого генератора)
     */
    fun getModelPath(context: Context): String {
        return ""
    }

    /**
     * Показывает прогресс загрузки (всегда 100% для простого генератора)
     */
    fun getDownloadProgress(): Int {
        return 100
    }
}