package com.learningtutor.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.learningtutor.core.ml.IRTEngine
import com.learningtutor.core.ml.SpacedRepetition
import com.learningtutor.core.models.Question
import com.learningtutor.data.database.QuestionDao
import com.learningtutor.data.repositories.QuestionRepository
import com.learningtutor.data.repositories.UserRepository
import com.learningtutor.ui.models.QuestionUI
import com.learningtutor.ui.models.LearningUIState
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val questionDao: QuestionDao
) : ViewModel() {

    private val gson = Gson()

    private val _uiState = MutableStateFlow(LearningUIState())
    val uiState: StateFlow<LearningUIState> = _uiState.asStateFlow()

    private val TAG = "LearningViewModel"

    init {
        Log.d(TAG, "ViewModel создан")
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
            Log.d(TAG, "loadInitialQuestions начат")
            _uiState.update { it.copy(isLoading = true) }

            try {
                Log.d(TAG, "Шаг 1: Получаем пользователя...")
                // 1. Создаем пользователя, если его нет
                userRepository.createUserIfNotExists()
                Log.d(TAG, "Пользователь создан/получен")

                // 2. Загружаем вопросы для повторения
                Log.d(TAG, "Шаг 2: Получаем вопросы для повторения...")
                val reviewQuestions = questionRepository.getQuestionsForReview()
                Log.d(TAG, "Получено вопросов для повторения: ${reviewQuestions.size}")

                if (reviewQuestions.isNotEmpty()) {
                    val question = reviewQuestions.first()
                    Log.d(TAG, "Первый вопрос: ${question.text}")
                    val questionUI = convertToUI(question)

                    _uiState.update {
                        it.copy(
                            currentQuestion = questionUI,
                            currentTopic = question.topic,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    Log.d(TAG, "UI обновлен с вопросом")
                } else {
                    Log.d(TAG, "Нет вопросов для повторения, пробуем другой способ...")

                    // 3. Пробуем получить случайный вопрос
                    val topics = questionDao.getAllTopics()
                    Log.d(TAG, "Доступные темы: ${topics.size}")

                    if (topics.isNotEmpty()) {
                        val randomTopic = topics.random()
                        Log.d(TAG, "Выбрана тема: $randomTopic")

                        val randomQuestion = questionDao.getRandomByTopic(randomTopic)
                        if (randomQuestion != null) {
                            Log.d(TAG, "Случайный вопрос найден: ${randomQuestion.text}")
                            val questionUI = convertToUI(randomQuestion)

                            _uiState.update {
                                it.copy(
                                    currentQuestion = questionUI,
                                    currentTopic = randomQuestion.topic,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        } else {
                            Log.w(TAG, "Нет вопросов в теме $randomTopic")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Нет вопросов в выбранной теме"
                                )
                            }
                        }
                    } else {
                        Log.w(TAG, "Нет доступных тем")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Нет доступных тем для изучения"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка в loadInitialQuestions", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ошибка загрузки: ${e.localizedMessage}"
                    )
                }
            }

            Log.d(TAG, "loadInitialQuestions завершен")
            Log.d(TAG, "Текущее состояние: isLoading=${_uiState.value.isLoading}, question=${_uiState.value.currentQuestion?.text?.take(20)}...")
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
            val user = userRepository.getCurrentUserSync()
            user?.let {
                val newTheta = IRTEngine.updateThetaOneItem(
                    oldTheta = it.currentTheta,
                    difficulty = question.difficulty,
                    isCorrect = isCorrect
                )

                userRepository.updateUserTheta(user.id, newTheta)

                questionRepository.saveAnswer(
                    userId = user.id,
                    questionId = question.id,
                    isCorrect = isCorrect,
                    responseTimeMs = 5000 // Заглушка
                )

                updateRepetitionSchedule(question.topic, isCorrect, newTheta)

                _uiState.update { state ->
                    state.copy(
                        isAnswerSubmitted = true,
                        showExplanation = true,
                        currentTheta = newTheta,
                        correctAnswers = if (isCorrect) state.correctAnswers + 1 else state.correctAnswers,
                        totalQuestions = state.totalQuestions + 1,
                        accuracy = if (state.totalQuestions + 1 > 0) {
                            (if (isCorrect) state.correctAnswers + 1 else state.correctAnswers).toDouble() / (state.totalQuestions + 1)
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
            _uiState.update { state ->
                state.copy(
                    isAnswerSubmitted = false,
                    showExplanation = false,
                    selectedAnswer = null,
                    errorMessage = null
                )
            }

            // Загружаем следующий вопрос
            val nextQuestion = questionRepository.getNextQuestion()
            nextQuestion?.let { question ->
                val questionUI = convertToUI(question)
                _uiState.update { state ->
                    state.copy(
                        currentQuestion = questionUI,
                        currentTopic = question.topic,
                        errorMessage = null
                    )
                }
            } ?: run {
                // Если не удалось загрузить следующий вопрос
                loadInitialQuestions()
            }
        }
    }

    fun setTopic(topic: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    currentTopic = topic,
                    isLoading = true,
                    errorMessage = null
                )
            }

            // Загружаем вопросы по теме
            val topicQuestions = questionRepository.getQuestionsByTopic(topic)

            if (topicQuestions.isNotEmpty()) {
                val question = topicQuestions.first()
                val questionUI = convertToUI(question)
                _uiState.update { state ->
                    state.copy(
                        currentQuestion = questionUI,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "Нет вопросов по теме $topic"
                    )
                }
            }
        }
    }

    private fun convertToUI(question: Question): QuestionUI {
        // Безопасный парсинг JSON с fallback
        val options = try {
            val type = object : TypeToken<Array<String>>() {}.type
            val result: Array<String>? = gson.fromJson(question.optionsJson, type)
            result?.toList() ?: createFallbackOptions()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга JSON для вопроса ${question.id}", e)
            createFallbackOptions()
        }

        // Проверка корректности correctIndex
        val safeCorrectIndex = if (question.correctIndex in options.indices) {
            question.correctIndex
        } else {
            Log.w(TAG, "Некорректный correctIndex: ${question.correctIndex}, устанавливаем 0")
            0
        }

        return QuestionUI(
            id = question.id,
            text = question.text.ifBlank { "Вопрос без текста" },
            options = options,
            correctIndex = safeCorrectIndex,
            explanation = question.explanation.ifBlank { "Объяснение отсутствует" },
            difficulty = question.difficulty.coerceIn(0.1, 5.0),
            topic = question.topic.ifBlank { "Общая тема" }
        )
    }

    private fun createFallbackOptions(): List<String> {
        return listOf(
            "Вариант 1",
            "Вариант 2",
            "Вариант 3",
            "Вариант 4"
        )
    }
    fun loadTestQuestion() {
        viewModelScope.launch {
            Log.d("LearningViewModel", "Загружаем тестовый вопрос...")

            val testQuestion = QuestionUI(
                id = "test-1",
                text = "Чему равно 15 + 27?",
                options = listOf("42", "32", "52", "37"),
                correctIndex = 0,
                explanation = "15 + 27 = 42",
                difficulty = 1.0,
                topic = "Математика"
            )

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    currentQuestion = testQuestion,
                    currentTopic = "Математика",
                    errorMessage = null
                )
            }

            Log.d("LearningViewModel", "Тестовый вопрос загружен")
        }
    }

    fun retryLoading() {
        Log.d(TAG, "retryLoading вызван")
        loadInitialQuestions()
    }
}