package com.learningtutor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learningtutor.ui.models.QuestionUI

@Composable
fun QuestionCard(
    question: QuestionUI,
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