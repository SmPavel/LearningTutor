package com.learningtutor.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.learningtutor.ui.models.QuestionUI

@Composable
fun QuestionCard(
    question: QuestionUI,
    selectedAnswer: Int?,
    isAnswerSubmitted: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
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
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Варианты ответов
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedAnswer == index
                val isCorrect = index == question.correctIndex

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                if (!isAnswerSubmitted) {
                                    onAnswerSelected(index)
                                }
                            },
                            role = Role.RadioButton
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isAnswerSubmitted && isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                            isAnswerSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                            isSelected -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка отправки или следующий вопрос
            if (!isAnswerSubmitted) {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer != null
                ) {
                    Text("Проверить ответ")
                }
            } else {
                // Объяснение
                if (question.explanation.isNotBlank()) {
                    Text(
                        text = "Объяснение: ${question.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Следующий вопрос")
                }
            }
        }
    }
}