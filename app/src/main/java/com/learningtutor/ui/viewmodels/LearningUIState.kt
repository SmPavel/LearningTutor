package com.learningtutor.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class LearningUIState(
    val isLoading: Boolean = true,
    val currentQuestion: QuestionUI? = null,
    val currentTopic: String? = null,
    val selectedAnswer: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val showExplanation: Boolean = false,
    val currentTheta: Double = 0.0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val accuracy: Double = 0.0,
    val errorMessage: String? = null
)