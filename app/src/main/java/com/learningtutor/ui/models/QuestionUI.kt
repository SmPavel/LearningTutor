package com.learningtutor.ui.models

data class QuestionUI(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: Double,
    val topic: String
)