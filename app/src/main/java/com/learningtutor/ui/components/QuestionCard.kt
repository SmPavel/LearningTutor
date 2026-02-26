package com.learningtutor.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learningtutor.ui.models.QuestionUI

@Composable
fun QuestionCard(
    question: QuestionUI,
    selectedAnswer: Int?,
    isAnswerSubmitted: Boolean,
    isCorrect: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Тема
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = question.topic,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Вопрос
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Варианты ответов
            question.options.forEachIndexed { index, option ->
                OptionItemSimple(
                    index = index,
                    option = option,
                    isSelected = selectedAnswer == index,
                    isCorrectOption = index == question.correctIndex,
                    isAnswerSubmitted = isAnswerSubmitted,
                    onAnswerSelected = onAnswerSelected
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопки
            if (isAnswerSubmitted) {
                ResultMessage(
                    isCorrect = isCorrect,
                    explanation = question.explanation
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Следующий вопрос")
                }
            } else {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer != null
                ) {
                    Text("Проверить ответ")
                }
            }
        }
    }
}

@Composable
fun OptionItemSimple(
    index: Int,
    option: String,
    isSelected: Boolean,
    isCorrectOption: Boolean,
    isAnswerSubmitted: Boolean,
    onAnswerSelected: (Int) -> Unit
) {
    val backgroundColor = when {
        isAnswerSubmitted && isCorrectOption -> Color(0xFF81C784)  // светло-зеленый
        isAnswerSubmitted && isSelected && !isCorrectOption -> Color(0xFFE57373)  // светло-красный
        isSelected -> Color(0xFFBBDEFB)  // светло-синий
        else -> Color(0xFFF5F5F5)  // светло-серый
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isAnswerSubmitted) {
                    Modifier.clickable { onAnswerSelected(index) }
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${index + 1}. $option",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            if (isAnswerSubmitted && isCorrectOption) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)  // темно-зеленый
                )
            }
        }
    }
}

@Composable
fun ResultMessage(
    isCorrect: Boolean,
    explanation: String
) {
    Surface(
        color = if (isCorrect) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (isCorrect) "✅ Правильно!" else "❌ Неправильно",
                style = MaterialTheme.typography.titleMedium
            )

            if (explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}