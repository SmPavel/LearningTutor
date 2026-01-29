package com.learningtutor.data.repositories

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
        // Seed вопросы для каждой темы
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
                )
            )
        )
    }

    suspend fun initializeSeedQuestions() {
        // Проверяем, есть ли уже вопросы
        val count = questionDao.countAll()
        if (count == 0) {
            // Добавляем seed вопросы
            SEED_QUESTIONS.forEach { (topic, questions) ->
                questionDao.insertAll(questions)
            }
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

    // Геттер для UserRepository
    fun getUserRepository(): UserRepository = userRepository
}