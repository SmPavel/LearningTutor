package com.learningtutor.data.database

import androidx.room.*
import com.learningtutor.core.models.UserProgress
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ProgressDao {

    @Query("SELECT * FROM userprogress WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserProgress(userId: String): Flow<List<UserProgress>>

    @Query("""
        SELECT * FROM userprogress 
        WHERE userId = :userId 
        AND questionId = :questionId
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun getLastAnswer(userId: String, questionId: String): UserProgress?

    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN isCorrect = 1 THEN 1 ELSE 0 END) as correct,
            AVG(CASE WHEN isCorrect = 1 THEN responseTimeMs ELSE NULL END) as avgTimeCorrect,
            AVG(CASE WHEN isCorrect = 0 THEN responseTimeMs ELSE NULL END) as avgTimeWrong
        FROM userprogress 
        WHERE userId = :userId
    """)
    suspend fun getUserStats(userId: String): UserStats?

    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN up.isCorrect = 1 THEN 1 ELSE 0 END) as correct
        FROM userprogress up
        JOIN questions q ON up.questionId = q.id
        WHERE up.userId = :userId 
        AND q.topic = :topic
    """)
    suspend fun getTopicStats(userId: String, topic: String): TopicStats?

    @Query("""
        SELECT q.topic, 
               COUNT(*) as totalAnswered,
               SUM(CASE WHEN up.isCorrect = 1 THEN 1 ELSE 0 END) as correctAnswered,
               AVG(CASE WHEN up.isCorrect = 1 THEN 1.0 ELSE 0.0 END) as accuracy
        FROM userprogress up
        JOIN questions q ON up.questionId = q.id
        WHERE up.userId = :userId
        GROUP BY q.topic
        ORDER BY accuracy ASC
    """)
    suspend fun getWeakestTopics(userId: String): List<TopicProgress>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgress)

    @Delete
    suspend fun delete(progress: UserProgress)

    @Query("DELETE FROM userprogress WHERE userId = :userId AND questionId = :questionId")
    suspend fun deleteByUserAndQuestion(userId: String, questionId: String)
}

// DTO для статистики пользователя
data class UserStats(
    val total: Int,
    val correct: Int,
    val avgTimeCorrect: Double?,
    val avgTimeWrong: Double?
)

// DTO для статистики по теме
data class TopicStats(
    val total: Int,
    val correct: Int
)

// DTO для прогресса по темам
data class TopicProgress(
    val topic: String,
    val totalAnswered: Int,
    val correctAnswered: Int,
    val accuracy: Double
)