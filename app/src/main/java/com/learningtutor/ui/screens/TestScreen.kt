package com.learningtutor.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learningtutor.ui.viewmodels.LearningViewModel
import kotlinx.coroutines.delay

@Composable
fun TestScreen() {
    val viewModel: LearningViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Простой тест
    LaunchedEffect(Unit) {
        repeat(10) { i ->
            delay(1000)
            Log.d("TestScreen", "Tick $i: isLoading=${uiState.isLoading}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "isLoading: ${uiState.isLoading}\n" +
                    "error: ${uiState.errorMessage}\n" +
                    "question: ${uiState.currentQuestion?.text?.take(30)}",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}