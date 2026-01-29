package com.learningtutor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.learningtutor.core.ml.IRTEngine
import com.learningtutor.core.ml.SpacedRepetition
import com.learningtutor.core.models.Question
import com.learningtutor.data.repositories.QuestionRepository
import com.learningtutor.data.repositories.UserRepository
import com.learningtutor.ui.models.QuestionUI
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val gson = Gson()

    private val _uiState = MutableStateFlow(LearningUIState())
    val uiState: StateFlow<LearningUIState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadInitialQuestions()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(currentTheta = it.currentTheta)
                    }
                }
            }
        }
    }

    private fun loadInitialQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Загружаем вопросы для повторения
            val reviewQuestions = questionRepository.getQuestionsForReview()

            if (reviewQuestions.isNotEmpty()) {
                val question = reviewQuestions.first()
                val questionUI = convertToUI(question)
                _uiState.update {
                    it.copy(
                        currentQuestion = questionUI,
                        currentTopic = question.topic,
                        isLoading = false
                    )
                }
            } else {
                // Нет вопросов для повторения
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectAnswer(index: Int) {
        _uiState.update { it.copy(selectedAnswer = index) }
    }

    fun submitAnswer() {
        val currentState = _uiState.value
        val question = currentState.currentQuestion ?: return
        val selectedAnswer = currentState.selectedAnswer ?: return

        val isCorrect = selectedAnswer == question.correctIndex

        viewModelScope.launch {
            // Обновляем модель IRT
            val user = userRepository.getCurrentUserSync()
            user?.let {
                val newTheta = IRTEngine.updateThetaOneItem(
                    oldTheta = it.currentTheta,
                    difficulty = question.difficulty,
                    isCorrect = isCorrect
                )

                // Обновляем пользователя
                userRepository.updateUserTheta(user.id, newTheta)

                // Сохраняем прогресс
                questionRepository.saveAnswer(
                    userId = user.id,
                    questionId = question.id,
                    isCorrect = isCorrect,
                    responseTimeMs = 5000 // Заглушка
                )

                // Обновляем интервал повторения
                updateRepetitionSchedule(question.topic, isCorrect, newTheta)

                // Обновляем UI
                _uiState.update {
                    it.copy(
                        isAnswerSubmitted = true,
                        showExplanation = true,
                        currentTheta = newTheta,
                        correctAnswers = if (isCorrect) it.correctAnswers + 1 else it.correctAnswers,
                        totalQuestions = it.totalQuestions + 1,
                        accuracy = if (it.totalQuestions + 1 > 0) {
                            (if (isCorrect) it.correctAnswers + 1 else it.correctAnswers).toDouble() / (it.totalQuestions + 1)
                        } else 0.0
                    )
                }
            }
        }
    }

    private suspend fun updateRepetitionSchedule(
        topic: String,
        isCorrect: Boolean,
        theta: Double
    ) {
        // Используем правильный enum Quality
        val quality = if (isCorrect) {
            SpacedRepetition.Companion.Quality.GOOD
        } else {
            SpacedRepetition.Companion.Quality.FAILED
        }

        questionRepository.updateRepetitionSchedule(
            topic = topic,
            quality = quality,
            currentTheta = theta
        )
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(
                isAnswerSubmitted = false,
                showExplanation = false,
                selectedAnswer = null
            ) }

            // Загружаем следующий вопрос
            val nextQuestion = questionRepository.getNextQuestion()
            nextQuestion?.let { question ->
                val questionUI = convertToUI(question)
                _uiState.update {
                    it.copy(
                        currentQuestion = questionUI,
                        currentTopic = question.topic
                    )
                }
            }
        }
    }

    fun setTopic(topic: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(
                currentTopic = topic,
                isLoading = true
            ) }

            // Загружаем вопросы по теме
            val topicQuestions = questionRepository.getQuestionsByTopic(topic)

            if (topicQuestions.isNotEmpty()) {
                val question = topicQuestions.first()
                val questionUI = convertToUI(question)
                _uiState.update {
                    it.copy(
                        currentQuestion = questionUI,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun convertToUI(question: Question): QuestionUI {
        val options = try {
            gson.fromJson(question.optionsJson, Array<String>::class.java).toList()
        } catch (e: Exception) {
            listOf("Ошибка загрузки вариантов")
        }

        return QuestionUI(
            id = question.id,
            text = question.text,
            options = options,
            correctIndex = question.correctIndex,
            explanation = question.explanation,
            difficulty = question.difficulty,
            topic = question.topic
        )
    }
}