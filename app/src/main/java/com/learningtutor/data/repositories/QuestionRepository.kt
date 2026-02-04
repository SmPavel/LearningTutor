package com.learningtutor.data.repositories

import android.util.Log
import com.google.gson.Gson
import com.learningtutor.core.ml.SpacedRepetition
import com.learningtutor.core.models.*
import com.learningtutor.data.database.*
import com.learningtutor.ui.models.TopicWithProgressUI
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject

class QuestionRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val progressDao: ProgressDao,
    private val scheduleDao: ScheduleDao,
    private val userRepository: UserRepository
) {

    private val gson = Gson()

    companion object {
        private val SEED_QUESTIONS = mapOf(
            "Дроби" to listOf(
                Question(
                    text = "Сколько будет 1/2 + 1/4?",
                    optionsJson = """["3/4", "2/6", "1/6", "2/4"]""",
                    correctIndex = 0,
                    explanation = "Приводим к общему знаменателю 4: 2/4 + 1/4 = 3/4",
                    topic = "Дроби",
                    difficulty = 0.5,
                    generatedByAI = false
                ),
                Question(
                    text = "Что больше: 2/3 или 3/5?",
                    optionsJson = """["2/3", "3/5", "Одинаковы", "Нельзя сравнить"]""",
                    correctIndex = 0,
                    explanation = "Приводим к общему знаменателю 15: 2/3 = 10/15, 3/5 = 9/15. 10/15 > 9/15",
                    topic = "Дроби",
                    difficulty = 1.0,
                    generatedByAI = false
                ),
                Question(
                    text = "Сократите дробь 8/12",
                    optionsJson = """["2/3", "4/6", "1/3", "3/4"]""",
                    correctIndex = 0,
                    explanation = "Делим числитель и знаменатель на 4: 8÷4=2, 12÷4=3",
                    topic = "Дроби",
                    difficulty = 0.8,
                    generatedByAI = false
                )
            ),
            "Уравнения" to listOf(
                Question(
                    text = "Решите уравнение: 2x + 5 = 15",
                    optionsJson = """["x = 5", "x = 10", "x = 7.5", "x = 20"]""",
                    correctIndex = 0,
                    explanation = "2x = 15 - 5 = 10, x = 10 / 2 = 5",
                    topic = "Уравнения",
                    difficulty = 0.8,
                    generatedByAI = false
                ),
                Question(
                    text = "Решите: 3x - 7 = 8",
                    optionsJson = """["x = 5", "x = 3", "x = 7", "x = 4"]""",
                    correctIndex = 0,
                    explanation = "3x = 8 + 7 = 15, x = 15 / 3 = 5",
                    topic = "Уравнения",
                    difficulty = 1.0,
                    generatedByAI = false
                )
            ),
            "Геометрия" to listOf(
                Question(
                    text = "Площадь квадрата со стороной 5 см равна:",
                    optionsJson = """["25 см²", "20 см²", "30 см²", "15 см²"]""",
                    correctIndex = 0,
                    explanation = "Площадь квадрата = сторона² = 5² = 25",
                    topic = "Геометрия",
                    difficulty = 1.0,
                    generatedByAI = false
                ),
                Question(
                    text = "Периметр прямоугольника со сторонами 4 см и 6 см равен:",
                    optionsJson = """["20 см", "24 см", "10 см", "16 см"]""",
                    correctIndex = 0,
                    explanation = "Периметр = 2 × (4 + 6) = 2 × 10 = 20 см",
                    topic = "Геометрия",
                    difficulty = 1.2,
                    generatedByAI = false
                )
            ),
            "Проценты" to listOf(
                Question(
                    text = "Чему равно 15% от 200?",
                    optionsJson = """["30", "15", "20", "25"]""",
                    correctIndex = 0,
                    explanation = "15% от 200 = 200 × 0.15 = 30",
                    topic = "Проценты",
                    difficulty = 1.0,
                    generatedByAI = false
                ),
                Question(
                    text = "Если цена товара 1000 руб., а скидка 20%, сколько стоит товар?",
                    optionsJson = """["800 руб.", "200 руб.", "900 руб.", "850 руб."]""",
                    correctIndex = 0,
                    explanation = "Скидка = 1000 × 0.20 = 200 руб. Цена со скидкой = 1000 - 200 = 800 руб.",
                    topic = "Проценты",
                    difficulty = 1.5,
                    generatedByAI = false
                )
            )
        )
    }

    suspend fun initializeSeedQuestions() {
        try {
            // Проверяем, есть ли уже вопросы
            val count = questionDao.countAll()
            Log.d("QuestionRepository", "Всего вопросов в базе: $count")

            if (count == 0) {
                Log.d("QuestionRepository", "Добавляем seed вопросы...")

                // Добавляем seed вопросы
                SEED_QUESTIONS.forEach { (topic, questions) ->
                    questionDao.insertAll(questions)
                    Log.d("QuestionRepository", "Добавлено ${questions.size} вопросов по теме: $topic")
                }

                // Проверяем после добавления
                val afterCount = questionDao.countAll()
                Log.d("QuestionRepository", "После добавления: $afterCount вопросов")
            } else {
                Log.d("QuestionRepository", "В базе уже есть $count вопросов")

                // Проверяем темы
                val topics = questionDao.getAllTopics()
                Log.d("QuestionRepository", "Темы в базе: $topics")
            }
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Ошибка при инициализации seed вопросов", e)
        }
    }

    suspend fun getQuestionsForReview(): List<Question> {
        val now = Date()

        // 1. Получаем темы, которые нужно повторить
        val schedules = scheduleDao.getSchedulesDueForReview(now)

        if (schedules.isNotEmpty()) {
            return schedules.flatMap { schedule ->
                questionDao.getAIGeneratedQuestions(schedule.topic)
            }.take(10)
        }

        // 2. Если нет расписания, берем вопросы с низкой точностью
        val user = userRepository.getCurrentUserSync() ?: return emptyList()
        val weakTopics = progressDao.getWeakestTopics(user.id)

        if (weakTopics.isNotEmpty()) {
            val weakestTopic = weakTopics.first().topic
            return questionDao.getAIGeneratedQuestions(weakestTopic).take(5)
        }

        // 3. Возвращаем случайные вопросы
        return questionDao.getRandomQuestions(5)
    }

    suspend fun getNextQuestion(): Question? {
        // 1. Проверяем, есть ли вопросы для повторения
        val reviewQuestions = getQuestionsForReview()
        if (reviewQuestions.isNotEmpty()) {
            return reviewQuestions.shuffled().first()
        }

        // 2. Получаем текущего пользователя
        val user = userRepository.getCurrentUserSync() ?: return null

        // 3. Берем тему с самым старым повторением или новую
        val allTopics = questionDao.getAllTopics()
        if (allTopics.isEmpty()) return null

        // Находим тему, которую давно не повторяли
        val topic = findTopicForReview(user.id, allTopics) ?: allTopics.first()

        // 4. Берем новый вопрос по этой теме
        return getNewQuestionForTopic(user.id, topic)
    }

    private suspend fun findTopicForReview(userId: String, topics: List<String>): String? {
        // Ищем тему, которую не повторяли дольше всего
        for (topic in topics.shuffled()) {
            val schedule = scheduleDao.getScheduleByTopic(userId, topic)
            if (schedule == null) {
                return topic // Новая тема
            }

            // Если просрочено повторение
            if (schedule.nextReviewDate <= Date()) {
                return topic
            }
        }

        // Находим тему с самым далеким повторением
        return topics.minByOrNull { topic ->
            val schedule = scheduleDao.getScheduleByTopic(userId, topic)
            schedule?.nextReviewDate?.time ?: 0L
        }
    }

    private suspend fun getNewQuestionForTopic(userId: String, topic: String): Question? {
        // 1. Пробуем взять неотвеченный вопрос
        val newQuestions = questionDao.getNewQuestionsForUser(userId, topic, 5)
        if (newQuestions.isNotEmpty()) {
            return newQuestions.first()
        }

        // 2. Пробуем взять вопрос, на который отвечали неправильно
        val weekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        val incorrectQuestions = questionDao.getIncorrectQuestions(userId, topic, weekAgo, 5)
        if (incorrectQuestions.isNotEmpty()) {
            return incorrectQuestions.first()
        }

        // 3. Берем случайный вопрос по теме
        return questionDao.getRandomByTopic(topic)
    }

    suspend fun getQuestionsByTopic(topic: String): List<Question> {
        return questionDao.getAIGeneratedQuestions(topic) +
                questionDao.getSeedQuestions(topic)
    }

    suspend fun saveAnswer(
        userId: String,
        questionId: String,
        isCorrect: Boolean,
        responseTimeMs: Long
    ) {
        val progress = UserProgress(
            userId = userId,
            questionId = questionId,
            isCorrect = isCorrect,
            responseTimeMs = responseTimeMs
        )
        progressDao.insert(progress)
    }

    suspend fun updateRepetitionSchedule(
        topic: String,
        quality: SpacedRepetition.Companion.Quality,
        currentTheta: Double
    ) {
        val user = userRepository.getCurrentUserSync() ?: return

        val schedule = scheduleDao.getScheduleByTopic(user.id, topic)
            ?: RepetitionSchedule(
                userId = user.id,
                topic = topic,
                nextReviewDate = Date(),
                easeFactor = 2.5,
                intervalDays = 1,
                repetitionCount = 0
            )

        val result = SpacedRepetition.calculateNextReview(
            quality = quality,
            easeFactor = schedule.easeFactor,
            interval = schedule.intervalDays,
            repetitions = schedule.repetitionCount,
            theta = currentTheta
        )

        schedule.nextReviewDate = result.nextReviewDate
        schedule.easeFactor = result.newEaseFactor
        schedule.intervalDays = result.newInterval
        schedule.repetitionCount = result.repetitionCount

        scheduleDao.insert(schedule)
    }

    suspend fun getUserStats(userId: String): UserStats? {
        return progressDao.getUserStats(userId)
    }

    suspend fun getTopicStats(userId: String, topic: String): TopicStats? {
        return progressDao.getTopicStats(userId, topic)
    }
    suspend fun getAllTopics(): List<String> {
        return try {
            val topics = questionDao.getAllTopics()
            Log.d("QuestionRepository", "Получено тем: ${topics.size}, список: $topics")
            topics
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Ошибка получения тем", e)
            emptyList()
        }
    }

    suspend fun getAllTopicsWithProgress(userId: String): List<TopicWithProgressUI> {
        val topics = questionDao.getAllTopics()
        return topics.map { topic ->
            val stats = progressDao.getTopicStats(userId, topic)
            val schedule = scheduleDao.getScheduleByTopic(userId, topic)

            TopicWithProgressUI(
                topic = topic,
                totalQuestions = questionDao.countByTopic(topic),
                answered = stats?.total ?: 0,
                correct = stats?.correct ?: 0,
                nextReview = schedule?.nextReviewDate,
                mastery = if (stats?.total != null && stats.total > 0) {
                    stats.correct.toDouble() / stats.total
                } else 0.0
            )
        }
    }

    // === НОВЫЕ МЕТОДЫ ДЛЯ ГЕНЕРАЦИИ ===

    /**
     * Генерирует новый вопрос по теме (заглушка без LLM)
     */
    suspend fun generateNewQuestion(
        topic: String,
        difficulty: Double? = null
    ): Question? {
        val user = userRepository.getCurrentUserSync() ?: return null

        // Определяем сложность на основе уровня знаний пользователя
        val targetDifficulty = difficulty ?: calculateTargetDifficulty(user.currentTheta)

        // Создаем простой вопрос без LLM (пока)
        return createDefaultQuestion(topic, targetDifficulty)
    }

    /**
     * Получает или генерирует следующий вопрос
     */
    suspend fun getOrGenerateQuestion(topic: String): Question? {
        // 1. Пробуем взять существующий неотвеченный вопрос
        val user = userRepository.getCurrentUserSync() ?: return null
        val newQuestions = questionDao.getNewQuestionsForUser(user.id, topic, 1)
        if (newQuestions.isNotEmpty()) {
            return newQuestions.first()
        }

        // 2. Пробуем сгенерировать новый
        return generateNewQuestion(topic)
    }

    private fun calculateTargetDifficulty(userTheta: Double): Double {
        // Адаптивная сложность на основе уровня знаний
        return when {
            userTheta < -1.0 -> 0.5  // Очень низкий уровень
            userTheta < 0.0 -> 1.0   // Низкий уровень
            userTheta < 1.0 -> 2.0   // Средний уровень
            userTheta < 2.0 -> 3.0   // Выше среднего
            else -> 4.0              // Высокий уровень
        }
    }

    private fun createDefaultQuestion(topic: String, difficulty: Double): Question {
        // Проверяем, есть ли seed вопросы для этой темы
        val seedQuestion = SEED_QUESTIONS[topic]?.firstOrNull()

        return if (seedQuestion != null) {
            // Используем seed вопрос с обновленной сложностью
            seedQuestion.copy(difficulty = difficulty)
        } else {
            // Создаем простой вопрос по умолчанию
            when (topic) {
                "Алгебра" -> Question(
                    text = "Упростите выражение: 2a + 3a - a",
                    optionsJson = """["4a", "5a", "3a", "6a"]""",
                    correctIndex = 0,
                    explanation = "2a + 3a - a = (2 + 3 - 1)a = 4a",
                    topic = topic,
                    difficulty = difficulty,
                    generatedByAI = false
                )
                "Тригонометрия" -> Question(
                    text = "Чему равен sin(90°)?",
                    optionsJson = """["1", "0", "0.5", "√2/2"]""",
                    correctIndex = 0,
                    explanation = "sin(90°) = 1",
                    topic = topic,
                    difficulty = difficulty,
                    generatedByAI = false
                )
                else -> Question(
                    text = "Чему равно 7 × 8?",
                    optionsJson = """["56", "54", "58", "64"]""",
                    correctIndex = 0,
                    explanation = "7 × 8 = 56",
                    topic = topic,
                    difficulty = 1.0,
                    generatedByAI = false
                )
            }
        }
    }

    /**
     * Устанавливает менеджер генерации
     */
    fun setGenerationManager(manager: com.learningtutor.core.ml.GenerationManager) {
        this.generationManager = manager
    }

    private var generationManager: com.learningtutor.core.ml.GenerationManager? = null

    /**
     * Добавляет сгенерированный вопрос в базу данных
     */
    suspend fun insertGeneratedQuestion(question: Question) {
        questionDao.insert(question)
    }

    /**
     * Возвращает количество сгенерированных вопросов по теме
     */
    suspend fun countGeneratedQuestions(topic: String): Int {
        return questionDao.getAIGeneratedQuestions(topic).size
    }

    // Геттер для UserRepository
    fun getUserRepository(): UserRepository = userRepository
}