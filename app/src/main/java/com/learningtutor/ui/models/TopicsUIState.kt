package com.learningtutor.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class TopicsUIState(
    val isLoading: Boolean = true,
    val topics: List<TopicWithProgressUI> = emptyList(),
    val selectedTopic: String? = null,
    val errorMessage: String? = null
)