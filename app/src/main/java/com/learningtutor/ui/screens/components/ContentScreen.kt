package com.learningtutor.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learningtutor.ui.models.LearningUIState
import com.learningtutor.ui.screens.StatsHeader
import com.learningtutor.ui.viewmodels.LearningViewModel

@Composable
fun ContentScreen(
    uiState: LearningUIState,
    viewModel: LearningViewModel,
    onProgressClick: () -> Unit,
    onTopicsClick: () -> Unit
) {
    var showStats by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Верхняя панель со статистикой
        AnimatedVisibility(
            visible = showStats,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            StatsHeader(
                currentTheta = uiState.currentTheta,
                accuracy = uiState.accuracy,
                correctAnswers = uiState.correctAnswers,
                totalQuestions = uiState.totalQuestions
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка скрыть/показать статистику
        TextButton(
            onClick = { showStats = !showStats },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = if (showStats)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (showStats) "Скрыть статистику" else "Показать статистику")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Вопрос с анимацией появления
        uiState.currentQuestion?.let { question ->
            // Вычисляем isCorrect ТОЛЬКО если ответ отправлен
            val isCorrect = if (uiState.isAnswerSubmitted) {
                uiState.selectedAnswer == question.correctIndex
            } else {
                false // Временное значение, не будет использоваться пока isAnswerSubmitted = false
            }

            QuestionCard(
                question = question,
                selectedAnswer = uiState.selectedAnswer,
                isAnswerSubmitted = uiState.isAnswerSubmitted,
                isCorrect = isCorrect,
                onAnswerSelected = viewModel::selectAnswer,
                onSubmit = { viewModel.submitAnswer() },
                onNext = { viewModel.loadNextQuestion() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопки навигации
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onProgressClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Прогресс")
            }

            OutlinedButton(
                onClick = onTopicsClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Темы")
            }
        }
    }
}