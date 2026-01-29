package com.learningtutor.data.database

import androidx.room.*
import com.learningtutor.core.models.RepetitionSchedule
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM repetitionschedule WHERE userId = :userId")
    fun getUserSchedules(userId: String): Flow<List<RepetitionSchedule>>

    @Query("""
        SELECT * FROM repetitionschedule 
        WHERE userId = :userId 
        AND nextReviewDate <= :date
        ORDER BY nextReviewDate ASC
    """)
    fun getSchedulesDueForReview(userId: String, date: Date): Flow<List<RepetitionSchedule>>

    @Query("""
        SELECT * FROM repetitionschedule 
        WHERE nextReviewDate <= :date
        ORDER BY nextReviewDate ASC
    """)
    suspend fun getSchedulesDueForReview(date: Date): List<RepetitionSchedule>

    @Query("""
        SELECT * FROM repetitionschedule 
        WHERE userId = :userId 
        AND topic = :topic
        LIMIT 1
    """)
    suspend fun getScheduleByTopic(userId: String, topic: String): RepetitionSchedule?

    @Query("""
        SELECT * FROM repetitionschedule 
        WHERE topic = :topic
        LIMIT 1
    """)
    suspend fun getScheduleByTopic(topic: String): RepetitionSchedule?

    @Query("SELECT COUNT(*) FROM repetitionschedule WHERE nextReviewDate <= :date")
    suspend fun countDueReviews(date: Date): Int

    @Query("""
        SELECT topic, 
               MIN(nextReviewDate) as nextReview,
               COUNT(*) as dueCount
        FROM repetitionschedule 
        WHERE nextReviewDate <= :date
        GROUP BY topic
        ORDER BY nextReview ASC
    """)
    suspend fun getDueReviewsByTopic(date: Date): List<DueReviewsByTopic>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: RepetitionSchedule)

    @Update
    suspend fun update(schedule: RepetitionSchedule)

    @Delete
    suspend fun delete(schedule: RepetitionSchedule)

    @Query("DELETE FROM repetitionschedule WHERE userId = :userId AND topic = :topic")
    suspend fun deleteByUserAndTopic(userId: String, topic: String)

    @Query("""
        UPDATE repetitionschedule 
        SET nextReviewDate = :newDate 
        WHERE userId = :userId AND topic = :topic
    """)
    suspend fun rescheduleTopic(userId: String, topic: String, newDate: Date)
}

// DTO для просроченных повторений по темам
data class DueReviewsByTopic(
    val topic: String,
    val nextReview: Date,
    val dueCount: Int
)