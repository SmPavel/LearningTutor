package com.learningtutor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.learningtutor.data.repositories.QuestionRepository
import com.learningtutor.data.repositories.UserRepository
import com.learningtutor.ui.models.TopicWithProgressUI
import com.learningtutor.ui.models.UserStatsUI
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUIState())
    val uiState: StateFlow<ProgressUIState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    fun loadProgressData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val user = userRepository.getCurrentUserSync()
            user?.let {
                // Загружаем статистику пользователя
                val stats = questionRepository.getUserStats(it.id)
                val userStats = UserStatsUI(
                    totalQuestions = stats?.total ?: 0,
                    correctAnswers = stats?.correct ?: 0,
                    accuracy = if (stats?.total != null && stats.total > 0) {
                        stats.correct.toDouble() / stats.total
                    } else 0.0,
                    currentTheta = it.currentTheta,
                    avgResponseTime = stats?.avgTimeCorrect ?: 0.0
                )

                // Загружаем прогресс по темам
                val topicsProgress = questionRepository.getAllTopicsWithProgress(it.id)

                // Загружаем историю тета
                val thetaHistory = userRepository.getUserThetaHistory()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userStats = userStats,
                        topicsProgress = topicsProgress,
                        thetaHistory = thetaHistory
                    )
                }
            }
        }
    }

    fun selectTopic(topic: String) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUserSync() ?: return@launch

            val topicStats = questionRepository.getTopicStats(user.id, topic)
            val details = TopicDetails(
                topic = topic,
                mastery = if (topicStats?.total != null && topicStats.total > 0) {
                    topicStats.correct.toDouble() / topicStats.total
                } else 0.0,
                nextReview = null // Можно дополнить
            )

            _uiState.update {
                it.copy(
                    selectedTopic = topic,
                    topicDetails = details
                )
            }
        }
    }
}

// UI State для ProgressScreen
data class ProgressUIState(
    val isLoading: Boolean = false,
    val userStats: UserStatsUI? = null,
    val topicsProgress: List<TopicWithProgressUI> = emptyList(),
    val thetaHistory: List<Pair<Long, Double>> = emptyList(),
    val selectedTopic: String? = null,
    val topicDetails: TopicDetails? = null
)

data class TopicDetails(
    val topic: String,
    val mastery: Double,
    val nextReview: Date?,
    val difficultyHistory: List<Pair<Date, Double>> = emptyList(),
    val commonMistakes: List<String> = emptyList()
)