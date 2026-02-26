package com.learningtutor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.learningtutor.data.repositories.QuestionRepository
import com.learningtutor.data.repositories.UserRepository
import com.learningtutor.ui.models.ProgressUIState
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUIState())
    val uiState: StateFlow<ProgressUIState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    fun loadProgressData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val user = userRepository.getCurrentUserSync()

                if (user != null) {
                    // Загружаем статистику пользователя
                    val userStats = questionRepository.getUserStats(user.id)

                    // Загружаем историю theta
                    val thetaHistory = userRepository.getUserThetaHistory()

                    // Загружаем прогресс по темам
                    val topicsProgress = questionRepository.getAllTopicsWithProgress(user.id)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userName = user.name,
                            currentTheta = user.currentTheta,
                            totalQuestions = userStats?.total ?: 0,
                            correctAnswers = userStats?.correct ?: 0,
                            accuracy = if (userStats?.total != null && userStats.total > 0) {
                                userStats.correct.toDouble() / userStats.total
                            } else 0.0,
                            thetaHistory = thetaHistory,
                            topicsProgress = topicsProgress,
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
                        errorMessage = "Ошибка загрузки данных: ${e.message}"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadProgressData()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}