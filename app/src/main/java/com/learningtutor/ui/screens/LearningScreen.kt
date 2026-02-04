package com.learningtutor.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.learningtutor.ui.models.LearningUIState
import com.learningtutor.ui.screens.components.*
import com.learningtutor.ui.viewmodels.LearningViewModel
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    onProgressClick: () -> Unit,
    onTopicsClick: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d("LearningScreen", "Composable запущен")
        Log.d("LearningScreen", "Initial state: isLoading=${uiState.isLoading}")
    }

    // Отладочная информация
    SideEffect {
        Log.d("LearningScreen", "State обновлен: isLoading=${uiState.isLoading}, error=${uiState.errorMessage}")
    }

    // Временный байпас: если долго грузится
    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            delay(3000) // Ждем 3 секунды
            if (uiState.isLoading && uiState.currentQuestion == null) {
                Log.w("LearningScreen", "Таймаут загрузки, показываем тестовый вопрос")
                viewModel.loadTestQuestion()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Умный репетитор") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Отладочный текст
            Text(
                text = "Debug: isLoading=${uiState.isLoading}, error=${uiState.errorMessage}",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )

            when {
                uiState.isLoading -> {
                    FullScreenLoader()
                }

                uiState.errorMessage != null -> {
                    ErrorScreen(
                        error = uiState.errorMessage!!,
                        onRetry = { viewModel.retryLoading() }
                    )
                }

                uiState.currentQuestion != null -> {
                    ContentScreen(
                        uiState = uiState,
                        viewModel = viewModel,
                        onProgressClick = onProgressClick,
                        onTopicsClick = onTopicsClick
                    )
                }

                else -> {
                    ErrorScreen(
                        error = "Нет доступных вопросов",
                        onRetry = { viewModel.retryLoading() }
                    )
                }
            }
        }
    }
}
@Composable
fun FullScreenLoader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Загрузка вопросов...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}