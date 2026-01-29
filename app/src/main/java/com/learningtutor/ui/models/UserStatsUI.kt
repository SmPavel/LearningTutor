package com.learningtutor.ui.models

data class UserStatsUI(
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Double = 0.0,
    val currentTheta: Double = 0.0,
    val avgResponseTime: Double = 0.0,
    val totalLearningTime: Long = 0
)

data class TopicWithProgressUI(
    val topic: String,
    val totalQuestions: Int,
    val answered: Int,
    val correct: Int,
    val nextReview: java.util.Date?,
    val mastery: Double
)