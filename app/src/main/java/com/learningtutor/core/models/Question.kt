package com.learningtutor.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val optionsJson: String, // JSON массив вариантов
    val correctIndex: Int,
    val explanation: String,
    val topic: String,
    val difficulty: Double, // Параметр b из IRT
    val subject: String = "Математика",
    val generatedByAI: Boolean = true,
    val aiPromptHash: String? = null,
    val createdAt: Date = Date()
)