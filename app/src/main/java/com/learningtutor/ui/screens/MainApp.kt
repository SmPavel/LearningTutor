package com.learningtutor.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.learningtutor.ui.viewmodels.LearningViewModel
import com.learningtutor.ui.viewmodels.ProgressViewModel
import com.learningtutor.ui.viewmodels.TopicsViewModel

@Composable
fun LearningApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "learning"
    ) {
        composable("learning") {
            val viewModel: LearningViewModel = hiltViewModel()

            // Перехватываем выбранную тему при возврате с экрана тем
            val backStackEntry = remember { navController.currentBackStackEntry }
            LaunchedEffect(backStackEntry) {
                // Проверяем, не вернулись ли мы с экрана тем с выбранной темой
                val selectedTopic = backStackEntry?.savedStateHandle?.get<String>("selected_topic")
                selectedTopic?.let {
                    viewModel.setTopic(it)
                    backStackEntry.savedStateHandle.remove<String>("selected_topic")
                }
            }

            LearningScreen(
                onProgressClick = { navController.navigate("progress") },
                onTopicsClick = { navController.navigate("topics") },
                viewModel = viewModel
            )
        }

        composable("progress") {
            val viewModel: ProgressViewModel = hiltViewModel()
            ProgressScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable("topics") {
            val viewModel: TopicsViewModel = hiltViewModel()
            TopicsScreen(
                onBackClick = { navController.popBackStack() },
                onTopicSelected = { topic ->
                    // Сохраняем выбранную тему в savedStateHandle текущего back stack entry
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_topic", topic)
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}