package com.learningtutor.data.database

import androidx.room.*
import com.learningtutor.core.models.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE topic = :topic ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByTopic(topic: String): Question?

    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getById(questionId: String): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: Question)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    @Query("SELECT * FROM questions WHERE generatedByAI = 1 AND topic = :topic")
    suspend fun getAIGeneratedQuestions(topic: String): List<Question>

    @Query("SELECT * FROM questions WHERE generatedByAI = 0 AND topic = :topic")
    suspend fun getSeedQuestions(topic: String): List<Question>

    @Query("SELECT COUNT(*) FROM questions WHERE topic = :topic")
    suspend fun countByTopic(topic: String): Int

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun countAll(): Int

    @Query("SELECT DISTINCT topic FROM questions")
    suspend fun getAllTopics(): List<String>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int): List<Question>

    @Query("""
        SELECT * FROM questions 
        WHERE id NOT IN (
            SELECT questionId FROM userprogress WHERE userId = :userId
        )
        AND topic = :topic
        ORDER BY RANDOM() 
        LIMIT :limit
    """)
    suspend fun getNewQuestionsForUser(userId: String, topic: String, limit: Int): List<Question>

    @Query("""
        SELECT q.* FROM questions q
        JOIN userprogress up ON q.id = up.questionId
        WHERE up.userId = :userId 
        AND up.isCorrect = 0
        AND up.timestamp >= :sinceDate
        AND q.topic = :topic
        ORDER BY up.timestamp DESC
        LIMIT :limit
    """)
    suspend fun getIncorrectQuestions(
        userId: String,
        topic: String,
        sinceDate: Long,
        limit: Int
    ): List<Question>

    @Delete
    suspend fun delete(question: Question)

    @Query("DELETE FROM questions WHERE topic = :topic")
    suspend fun deleteByTopic(topic: String)
}