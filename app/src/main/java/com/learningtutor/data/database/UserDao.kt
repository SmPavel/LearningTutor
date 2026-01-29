package com.learningtutor.data.database

import androidx.room.*
import com.learningtutor.core.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("UPDATE user SET currentTheta = :theta WHERE id = :userId")
    suspend fun updateTheta(userId: String, theta: Double)

    @Query("UPDATE user SET thetaHistoryJson = :historyJson WHERE id = :userId")
    suspend fun updateThetaHistory(userId: String, historyJson: String)

    @Query("DELETE FROM user")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM user")
    suspend fun count(): Int
}