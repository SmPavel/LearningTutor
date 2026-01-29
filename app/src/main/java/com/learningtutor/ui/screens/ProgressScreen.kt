package com.learningtutor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.learningtutor.ui.models.TopicWithProgressUI
import com.learningtutor.ui.models.UserStatsUI
import com.learningtutor.ui.viewmodels.ProgressViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBackClick: () -> Unit,
    viewModel: ProgressViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Прогресс обучения") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Общая статистика
                item {
                    UserStatsCard(uiState.userStats)
                }

                // Темы
                item {
                    Text(
                        text = "Темы",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.topicsProgress) { topic ->
                    TopicProgressCard(
                        topic = topic,
                        onClick = { viewModel.selectTopic(topic.topic) }
                    )
                }

                // История тета
                item {
                    ThetaHistoryCard(uiState.thetaHistory)
                }
            }
        }
    }
}

@Composable
fun UserStatsCard(stats: UserStatsUI?) {
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
                text = "Общая статистика",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.ShowChart,
                    label = "Уровень θ",
                    value = String.format("%.2f", stats?.currentTheta ?: 0.0)
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Точность",
                    value = String.format("%.0f%%", (stats?.accuracy ?: 0.0) * 100)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Решено",
                    value = "${stats?.correctAnswers ?: 0}/${stats?.totalQuestions ?: 0}"
                )
                StatItem(
                    icon = Icons.Default.Timer,
                    label = "Среднее время",
                    value = String.format("%.1fс", (stats?.avgResponseTime ?: 0.0) / 1000)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicProgressCard(
    topic: TopicWithProgressUI,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = topic.topic,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Решено: ${topic.answered}/${topic.totalQuestions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Индикатор мастерства
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = topic.mastery.toFloat(),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "${(topic.mastery * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Прогресс бар
            LinearProgressIndicator(
                progress = topic.mastery.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // Следующее повторение
            topic.nextReview?.let {
                val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                Text(
                    text = "Повторить: ${formatter.format(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ThetaHistoryCard(history: List<Pair<Long, Double>>) {
    if (history.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Динамика уровня знаний (θ)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Простой график
            SimpleThetaChart(history)

            // Последнее значение
            val lastTheta = history.lastOrNull()?.second ?: 0.0
            Text(
                text = "Текущий уровень: ${String.format("%.2f", lastTheta)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun SimpleThetaChart(history: List<Pair<Long, Double>>) {
    // Здесь должна быть реализация графика
    // Для простоты покажем текстовое представление
    val recentHistory = history.takeLast(5)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        recentHistory.forEach { (timestamp, theta) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("dd.MM", Locale.getDefault())
                        .format(Date(timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = String.format("θ = %.2f", theta),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}