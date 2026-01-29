// app/src/main/java/com/learningtutor/core/ml/SpacedRepetition.kt
package com.learningtutor.core.ml

import java.util.*
import kotlin.math.max

/**
 * Реализация алгоритма SM-2 для интервального повторения
 */
class SpacedRepetition {

    data class ReviewResult(
        val nextReviewDate: Date,
        val newEaseFactor: Double,
        val newInterval: Int,
        val repetitionCount: Int
    )

    companion object {
        // Качество ответа (0-5)
        enum class Quality(val value: Int) {
            FAILED(0),           // Совсем не вспомнил
            HARD(1),            // Вспомнил с трудом, ошибся
            DIFFICULT(2),       // Вспомнил, но с ошибками
            GOOD(3),            // Правильно, но с задержкой
            EASY(4),            // Правильно, почти мгновенно
            PERFECT(5)          // Идеально, без раздумий
        }

        /**
         * Основная функция алгоритма SM-2
         * @param quality качество ответа (0-5)
         * @param easeFactor текущий фактор простоты (EF)
         * @param interval текущий интервал в днях
         * @param repetitions текущее количество повторений
         * @param theta уровень знаний для коррекции
         */
        fun calculateNextReview(
            quality: Quality,
            easeFactor: Double,
            interval: Int,
            repetitions: Int,
            theta: Double = 1.0 // Нормализованный уровень знаний
        ): ReviewResult {
            var newEaseFactor = easeFactor
            var newInterval = interval
            var newRepetitions = repetitions

            // Коррекция качества на основе уровня знаний
            val adjustedQuality = adjustQualityForTheta(quality, theta)

            if (adjustedQuality.value < 3) {
                // Неправильный ответ - начинаем заново
                newRepetitions = 0
                newInterval = 1
            } else {
                // Обновляем фактор простоты
                newEaseFactor = updateEaseFactor(easeFactor, adjustedQuality.value)

                // Ограничиваем EF
                newEaseFactor = newEaseFactor.coerceIn(1.3, 2.5)

                if (repetitions == 0) {
                    newInterval = 1
                } else if (repetitions == 1) {
                    newInterval = 6
                } else {
                    newInterval = (interval * newEaseFactor).toInt()
                }

                newRepetitions = repetitions + 1
            }

            // Коррекция интервала на основе уровня знаний
            newInterval = adjustIntervalForTheta(newInterval, theta).toInt()

            // Создаем дату следующего повторения
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, newInterval)
            val nextReviewDate = calendar.time

            return ReviewResult(
                nextReviewDate = nextReviewDate,
                newEaseFactor = newEaseFactor,
                newInterval = newInterval,
                repetitionCount = newRepetitions
            )
        }

        /**
         * Обновление фактора простоты по формуле SM-2
         */
        private fun updateEaseFactor(oldEF: Double, quality: Int): Double {
            return oldEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        }

        /**
         * Коррекция качества ответа на основе уровня знаний
         */
        private fun adjustQualityForTheta(quality: Quality, theta: Double): Quality {
            if (theta < 0.3) {
                // Низкий уровень знаний - снижаем требования
                return when (quality) {
                    Quality.FAILED -> Quality.HARD
                    Quality.HARD -> Quality.DIFFICULT
                    else -> quality
                }
            } else if (theta > 1.5) {
                // Высокий уровень знаний - повышаем требования
                return when (quality) {
                    Quality.PERFECT -> Quality.EASY
                    Quality.EASY -> Quality.GOOD
                    else -> quality
                }
            }
            return quality
        }

        /**
         * Коррекция интервала на основе уровня знаний
         */
        private fun adjustIntervalForTheta(interval: Int, theta: Double): Double {
            val targetTheta = 1.0
            val diff = theta - targetTheta

            return if (diff < -0.5) {
                // Знания слабые - сокращаем интервал
                interval * 0.5
            } else if (diff < -0.2) {
                interval * 0.8
            } else if (diff > 0.5) {
                // Знания отличные - увеличиваем интервал
                interval * 1.5
            } else if (diff > 0.2) {
                interval * 1.2
            } else {
                interval.toDouble()
            }
        }
    }
}