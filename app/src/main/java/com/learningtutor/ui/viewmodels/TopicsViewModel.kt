package com.learningtutor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.learningtutor.data.repositories.QuestionRepository
import com.learningtutor.data.repositories.UserRepository
import com.learningtutor.ui.models.TopicsUIState
import javax.inject.Inject

@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TopicsUIState())
    val uiState: StateFlow<TopicsUIState> = _uiState.asStateFlow()

    init {
        loadTopics()
    }

    fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val user = userRepository.getCurrentUserSync()

                if (user != null) {
                    val topicsProgress = questionRepository.getAllTopicsWithProgress(user.id)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            topics = topicsProgress,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Пользователь не найден"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки тем: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectTopic(topic: String) {
        _uiState.update { it.copy(selectedTopic = topic) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}