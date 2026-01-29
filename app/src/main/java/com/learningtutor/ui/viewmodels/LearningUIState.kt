package com.learningtutor.ui.viewmodels

import com.learningtutor.ui.models.QuestionUI

data class LearningUIState(
    val currentQuestion: QuestionUI? = null,
    val currentTopic: String? = null,
    val topicMastery: Double = 0.0,
    val currentTheta: Double = 0.0,
    val selectedAnswer: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val showExplanation: Boolean = false,
    val showStats: Boolean = true,
    val isLoading: Boolean = false,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Double = 0.0
)