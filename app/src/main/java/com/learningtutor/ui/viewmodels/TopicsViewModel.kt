package com.learningtutor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.learningtutor.data.repositories.QuestionRepository
import com.learningtutor.ui.models.TopicWithProgressUI
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicsUIState())
    val uiState: StateFlow<TopicsUIState> = _uiState.asStateFlow()

    init {
        loadTopics()
    }

    fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val user = questionRepository.getUserRepository().getCurrentUserSync()
            user?.let {
                val topics = questionRepository.getAllTopicsWithProgress(it.id)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topics = topics
                    )
                }
            }
        }
    }

    fun selectTopic(topic: String) {
        _uiState.update { it.copy(selectedTopic = topic) }
    }
}

data class TopicsUIState(
    val isLoading: Boolean = false,
    val topics: List<TopicWithProgressUI> = emptyList(),
    val selectedTopic: String? = null
)