package com.learningtutor.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID
@Entity(tableName = "userprogress")
data class UserProgress(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val questionId: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val timestamp: Date = Date()
)