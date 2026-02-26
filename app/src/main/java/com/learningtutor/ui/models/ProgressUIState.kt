package com.learningtutor.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class ProgressUIState(
    val isLoading: Boolean = true,
    val userName: String = "Ученик",
    val currentTheta: Double = 0.0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val accuracy: Double = 0.0,
    val thetaHistory: List<Pair<Long, Double>> = emptyList(),
    val topicsProgress: List<TopicWithProgressUI> = emptyList(),
    val errorMessage: String? = null
)