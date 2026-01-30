// app/src/main/java/com/learningtutor/core/ml/SimpleLLMGenerator.kt
package com.learningtutor.core.ml

import android.content.Context
import com.google.gson.Gson
import com.learningtutor.core.models.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.pow
import kotlin.random.Random

/**
 * Простой генератор вопросов без C++ и JNI
 */
class SimpleLLMGenerator(private val context: Context) {

    private val gson = Gson()
    private val random = Random(System.currentTimeMillis())

    // База знаний вопросов по темам
    private val questionTemplates = mapOf(
        "Дроби" to listOf(
            Triple("Сколько будет \${a}/\${b} + \${c}/\${d}?", "Приводим к общему знаменателю", 1.0),
            Triple("Сократите дробь \${a}/\${b}", "Делим числитель и знаменатель на НОД", 1.5),
            Triple("Сравните дроби: \${a}/\${b} и \${c}/\${d}", "Приводим к общему знаменателю и сравниваем", 2.0),
            Triple("Вычислите: \${a}/\${b} × \${c}/\${d}", "Умножаем числители и знаменатели", 2.5),
            Triple("Разделите: \${a}/\${b} ÷ \${c}/\${d}", "Умножаем на обратную дробь", 3.0)
        ),
        "Уравнения" to listOf(
            Triple("Решите: x + \${a} = \${b}", "x = \${b} - \${a}", 1.0),
            Triple("Решите: \${a}x - \${b} = \${c}", "\${a}x = \${c} + \${b}, x = (\${c} + \${b}) / \${a}", 1.5),
            Triple("Решите: \${a}(x + \${b}) = \${c}", "x + \${b} = \${c} / \${a}, x = (\${c} / \${a}) - \${b}", 2.0),
            Triple("Решите: (\${a}x)/\${b} = \${c}", "\${a}x = \${c} × \${b}, x = (\${c} × \${b}) / \${a}", 2.5),
            Triple("Решите систему: {x + y = \${a}, x - y = \${b}}", "Складываем: 2x = \${a} + \${b}, x = (\${a} + \${b}) / 2", 3.0)
        ),
        "Геометрия" to listOf(
            Triple("Площадь квадрата со стороной \${a} см равна:", "Площадь = сторона² = \${a}² = \${a*a}", 1.0),
            Triple("Периметр прямоугольника \${a}×\${b} см равен:", "Периметр = 2×(a+b) = 2×(\${a}+\${b}) = \${2*(a+b)}", 1.5),
            Triple("Площадь круга с радиусом \${a} см (π≈3.14):", "Площадь = πr² = 3.14×\${a}² = \${3.14*a*a}", 2.0),
            Triple("Объем куба с ребром \${a} см равен:", "Объем = ребро³ = \${a}³ = \${a*a*a}", 2.5),
            Triple("Площадь треугольника: основание \${a} см, высота \${b} см:", "Площадь = (a×b)/2 = (\${a}×\${b})/2 = \${(a*b)/2.0}", 3.0)
        ),
        "Проценты" to listOf(
            Triple("Чему равно \${a}% от \${b}?", "\${a}% от \${b} = \${b} × 0.\${a} = \${b*(a/100.0)}", 1.0),
            Triple("Товар стоит \${a} руб. со скидкой \${b}%. Цена до скидки?", "x × (1 - 0.\${b}) = \${a}, x = \${a} / (1 - 0.\${b}) ≈ \${a/(1-b/100.0)}", 2.0),
            Triple("Число увеличили на \${a}%, получилось \${b}. Исходное число?", "x × 1.\${a} = \${b}, x = \${b} / 1.\${a} = \${b/(1+a/100.0)}", 2.5),
            Triple("Вклад \${a} руб. под \${b}% годовых. Сумма через \${years} года?", "Сумма = \${a} × (1 + 0.\${b})^\${years} = \${a} × \${1+b/100.0}^\${years} ≈ \${a*Math.pow(1+b/100.0, years.toDouble())}", 3.0)
        )
    )

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        delay(100) // Симулируем короткую инициализацию
        true
    }

    suspend fun generateQuestion(
        topic: String,
        difficulty: Double,
        subject: String = "math"
    ): Result<Question> = withContext(Dispatchers.IO) {
        try {
            delay(100) // Симулируем время генерации

            val templates = questionTemplates[topic] ?: questionTemplates["Дроби"]!!

            // Выбираем шаблон в зависимости от сложности
            val difficultyLevel = difficulty.coerceIn(1.0, 5.0).toInt()
            val templateIndex = ((difficultyLevel - 1) * templates.size / 5).coerceIn(0, templates.size - 1)
            val template = templates[templateIndex]

            // Генерируем случайные числа для шаблона
            val numbers = generateNumbers(difficultyLevel)

            // Заполняем шаблон
            val questionText = fillTemplate(template.first, numbers, difficultyLevel)
            val explanation = fillTemplate(template.second, numbers, difficultyLevel)

            // Генерируем варианты ответов
            val correctAnswer = calculateAnswer(template, numbers, difficultyLevel)
            val options = generateOptions(correctAnswer, topic, difficultyLevel)

            val question = Question(
                text = questionText,
                optionsJson = gson.toJson(options),
                correctIndex = 0, // Правильный ответ всегда первый
                explanation = explanation,
                topic = topic,
                difficulty = difficulty,
                generatedByAI = true,
                aiPromptHash = "${topic}_${difficulty}_${Date().time}".hashCode().toString()
            )

            Result.success(question)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanup() {
        // Ничего не делаем для простого генератора
    }

    private fun generateNumbers(difficulty: Int): List<Int> {
        // Для шаблона с годами добавляем третий параметр
        return when (difficulty) {
            1 -> listOf(
                random.nextInt(1, 6),
                random.nextInt(1, 6),
                random.nextInt(1, 6),
                random.nextInt(1, 6),
                2 // years
            )
            2 -> listOf(
                random.nextInt(1, 10),
                random.nextInt(1, 10),
                random.nextInt(1, 10),
                random.nextInt(1, 10),
                3 // years
            )
            3 -> listOf(
                random.nextInt(1, 15),
                random.nextInt(1, 15),
                random.nextInt(1, 15),
                random.nextInt(1, 15),
                4 // years
            )
            4 -> listOf(
                random.nextInt(1, 20),
                random.nextInt(1, 20),
                random.nextInt(1, 20),
                random.nextInt(1, 20),
                5 // years
            )
            else -> listOf(
                random.nextInt(1, 25),
                random.nextInt(1, 25),
                random.nextInt(1, 25),
                random.nextInt(1, 25),
                5 // years
            )
        }
    }

    private fun fillTemplate(template: String, numbers: List<Int>, difficulty: Int): String {
        var result = template

        // Заменяем переменные числами
        numbers.forEachIndexed { index, number ->
            result = result.replace("\${${index + 1}}", number.toString())
        }

        // Заменяем именованные переменные
        val a = numbers[0]
        val b = numbers[1]
        val c = numbers[2]
        val d = numbers[3]
        val years = numbers.getOrNull(4) ?: 2

        result = result.replace("\${a}", a.toString())
        result = result.replace("\${b}", b.toString())
        result = result.replace("\${c}", c.toString())
        result = result.replace("\${d}", d.toString())
        result = result.replace("\${years}", years.toString())

        // Вычисляем выражения
        result = result.replace("\${a*a}", (a * a).toString())
        result = result.replace("\${2*(a+b)}", (2 * (a + b)).toString())
        result = result.replace("\${3.14*a*a}", (3.14 * a * a).toString())
        result = result.replace("\${a*a*a}", (a * a * a).toString())
        result = result.replace("\${(a*b)/2.0}", ((a * b) / 2.0).toString())
        result = result.replace("\${b*(a/100.0)}", (b * (a / 100.0)).toString())
        result = result.replace("\${a/(1-b/100.0)}", (a / (1 - b / 100.0)).toString())
        result = result.replace("\${b/(1+a/100.0)}", (b / (1 + a / 100.0)).toString())
        result = result.replace("\${1+b/100.0}", (1 + b / 100.0).toString())
        result = result.replace("\${a*Math.pow(1+b/100.0, years.toDouble())}",
            (a * Math.pow(1 + b / 100.0, years.toDouble())).toString())

        return result
    }

    private fun calculateAnswer(
        template: Triple<String, String, Double>,
        numbers: List<Int>,
        difficulty: Int
    ): String {
        // Для простоты возвращаем "Ответ 1"
        return when (difficulty) {
            1 -> "Ответ 1"
            2 -> "Ответ 2"
            3 -> "Ответ 3"
            4 -> "Ответ 4"
            else -> "Правильный ответ"
        }
    }

    private fun generateOptions(correctAnswer: String, topic: String, difficulty: Int): List<String> {
        val options = mutableListOf(correctAnswer)

        // Генерируем неправильные варианты
        when (topic) {
            "Дроби" -> {
                for (i in 1..3) {
                    val num = random.nextInt(1, 10)
                    val den = random.nextInt(2, 10)
                    options.add("$num/$den")
                }
            }
            "Уравнения" -> {
                for (i in 1..3) {
                    val x = random.nextInt(1, 10)
                    options.add("x = $x")
                }
            }
            "Геометрия" -> {
                for (i in 1..3) {
                    val area = random.nextInt(10, 100)
                    options.add("$area см${if (difficulty >= 3) "³" else "²"}")
                }
            }
            "Проценты" -> {
                for (i in 1..3) {
                    val percent = random.nextInt(10, 100)
                    options.add("$percent")
                }
            }
            else -> {
                for (i in 1..3) {
                    options.add("Вариант ${i + 1}")
                }
            }
        }

        return options.shuffled()
    }
}