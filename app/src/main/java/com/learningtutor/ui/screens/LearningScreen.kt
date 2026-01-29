package com.learningtutor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learningtutor.ui.viewmodels.LearningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    onProgressClick: () -> Unit,
    onTopicsClick: () -> Unit,
    viewModel: LearningViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Умный репетитор") },
                actions = {
                    IconButton(onClick = onProgressClick) {
                        Icon(Icons.Default.BarChart, "Прогресс")
                    }
                    IconButton(onClick = onTopicsClick) {
                        Icon(Icons.Default.List, "Темы")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Текущая тема
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Текущая тема",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = uiState.currentTopic ?: "Не выбрана",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Уровень знаний по теме
                    LinearProgressIndicator(
                        progress = uiState.topicMastery.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                    Text(
                        text = "Уровень знаний: ${(uiState.topicMastery * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Текущее задание
            if (uiState.currentQuestion != null) {
                QuestionCard(
                    question = uiState.currentQuestion!!,
                    selectedAnswer = uiState.selectedAnswer,
                    onAnswerSelected = viewModel::selectAnswer,
                    onSubmit = { viewModel.submitAnswer() },
                    isSubmitted = uiState.isAnswerSubmitted,
                    showExplanation = uiState.showExplanation,
                    onNextQuestion = viewModel::loadNextQuestion
                )
            } else {
                // Загрузка или нет тем
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Выберите тему для начала обучения",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onTopicsClick) {
                                Text("Выбрать тему")
                            }
                        }
                    }
                }
            }

            // Статистика
            if (uiState.showStats) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Статистика",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("θ", String.format("%.2f", uiState.currentTheta))
                            StatItem("Всего", uiState.totalQuestions.toString())
                            StatItem("Верно", uiState.correctAnswers.toString())
                            StatItem("%", String.format("%.0f%%", uiState.accuracy * 100))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: com.learningtutor.ui.models.QuestionUI,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    isSubmitted: Boolean,
    showExplanation: Boolean,
    onNextQuestion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Текст вопроса
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Варианты ответов
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(question.options.size) { index ->
                    val option = question.options[index]
                    val isCorrect = index == question.correctIndex

                    OutlinedButton(
                        onClick = { if (!isSubmitted) onAnswerSelected(index) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = when {
                                !isSubmitted && selectedAnswer == index ->
                                    MaterialTheme.colorScheme.primaryContainer
                                isSubmitted && isCorrect ->
                                    MaterialTheme.colorScheme.tertiaryContainer
                                isSubmitted && selectedAnswer == index && !isCorrect ->
                                    MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isSubmitted) {
                    Button(
                        onClick = onSubmit,
                        enabled = selectedAnswer != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Проверить")
                    }
                } else {
                    Button(
                        onClick = onNextQuestion,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Следующий вопрос")
                    }
                }
            }

            // Объяснение
            if (showExplanation && question.explanation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Объяснение: ${question.explanation}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}