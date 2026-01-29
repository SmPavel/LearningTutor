package com.learningtutor.data.repositories

import java.util.Date
data class TopicWithProgress(
    val topic: String,
    val totalQuestions: Int,
    val answered: Int,
    val correct: Int,
    val nextReview: Date?,
    val mastery: Double
)