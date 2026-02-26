package com.learningtutor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.learningtutor.ui.screens.LearningScreen
import com.learningtutor.ui.screens.ProgressScreen
import com.learningtutor.ui.screens.TopicsScreen

sealed class Screen(val route: String) {
    object Learning : Screen("learning")
    object Progress : Screen("progress")
    object Topics : Screen("topics")
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Learning.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Learning.route) {
            LearningScreen(
                onProgressClick = { navController.navigate(Screen.Progress.route) },
                onTopicsClick = { navController.navigate(Screen.Topics.route) }
            )
        }

        composable(Screen.Progress.route) {
            ProgressScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Topics.route) {
            TopicsScreen(
                onBackClick = { navController.popBackStack() },
                onTopicSelected = { topic ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_topic", topic)
                    navController.popBackStack()
                }
            )
        }
    }
}