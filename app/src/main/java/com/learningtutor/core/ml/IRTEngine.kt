package com.learningtutor.core.ml

import kotlin.math.exp
import kotlin.math.ln

/**
 * Реализация модели Раша (Item Response Theory)
 */
class IRTEngine {

    companion object {
        // Функция вероятности правильного ответа
        fun probabilityCorrect(theta: Double, difficulty: Double): Double {
            return 1.0 / (1.0 + exp(-(theta - difficulty)))
        }

        // Оценка максимального правдоподобия для одного ответа
        fun updateThetaOneItem(
            oldTheta: Double,
            difficulty: Double,
            isCorrect: Boolean,
            learningRate: Double = 1.0
        ): Double {
            val response = if (isCorrect) 1.0 else 0.0
            val p = probabilityCorrect(oldTheta, difficulty)

            // Градиент функции правдоподобия
            val residual = response - p

            // Информация Фишера (дисперсия)
            val information = p * (1 - p)

            // Обновление theta методом Ньютона-Рафсона
            val step = if (information > 0.0001) {
                residual / information
            } else {
                residual
            }

            // Ограничиваем шаг для стабильности
            val boundedStep = step.coerceIn(-0.5, 0.5) * learningRate

            return oldTheta + boundedStep
        }

        // Оценка theta по нескольким ответам
        fun estimateTheta(
            difficulties: List<Double>,
            responses: List<Boolean>,
            initialTheta: Double = 0.0,
            maxIterations: Int = 50,
            tolerance: Double = 0.001
        ): Double {
            var theta = initialTheta

            for (iteration in 0 until maxIterations) {
                var totalResidual = 0.0
                var totalInformation = 0.0

                for (i in difficulties.indices) {
                    val p = probabilityCorrect(theta, difficulties[i])
                    val response = if (responses[i]) 1.0 else 0.0

                    totalResidual += (response - p)
                    totalInformation += p * (1 - p)
                }

                if (totalInformation < 0.0001) break

                val step = totalResidual / totalInformation
                theta += step.coerceIn(-0.5, 0.5)

                if (Math.abs(step) < tolerance) break
            }

            return theta
        }

        // Стандартная ошибка измерения
        fun standardError(difficulties: List<Double>, theta: Double): Double {
            var totalInformation = 0.0

            for (difficulty in difficulties) {
                val p = probabilityCorrect(theta, difficulty)
                totalInformation += p * (1 - p)
            }

            return if (totalInformation > 0) {
                1.0 / kotlin.math.sqrt(totalInformation)
            } else {
                1.0
            }
        }
    }
}