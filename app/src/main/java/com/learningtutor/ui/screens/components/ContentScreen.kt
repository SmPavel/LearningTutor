package com.learningtutor.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.learningtutor.ui.models.LearningUIState
import com.learningtutor.ui.viewmodels.LearningViewModel

@Composable
fun ContentScreen(
    uiState: LearningUIState,
    viewModel: LearningViewModel,
    onProgressClick: () -> Unit,
    onTopicsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Заголовок и статистика
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.currentTopic ?: "Общая тема",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Статистика
            Row {
                Text(
                    text = "Точность: ${(uiState.accuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Θ: ${"%.2f".format(uiState.currentTheta)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Карточка с вопросом
        uiState.currentQuestion?.let { question ->
            QuestionCard(
                question = question,
                selectedAnswer = uiState.selectedAnswer,
                isAnswerSubmitted = uiState.isAnswerSubmitted,
                onAnswerSelected = viewModel::selectAnswer,
                onSubmit = { viewModel.submitAnswer() },
                onNext = { viewModel.loadNextQuestion() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Кнопки навигации
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onProgressClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Прогресс")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onTopicsClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Темы")
            }
        }
    }
}