package com.pandakind.steptrail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pandakind.steptrail.ui.detail.WalkDetailScreen
import com.pandakind.steptrail.ui.journal.JournalScreen
import com.pandakind.steptrail.ui.main.MainScreen
import com.pandakind.steptrail.ui.theme.StepTrailTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Главная активность приложения.
 * Настраивает навигацию. Запрос разрешений — в MainScreen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StepTrailTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StepTrailNavHost(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Навигационный граф приложения.
 */
@Composable
private fun StepTrailNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        composable("main") {
            val viewModel: com.pandakind.steptrail.ui.main.MainViewModel = hiltViewModel()
            MainScreen(
                viewModel = viewModel,
                onNavigateToJournal = { navController.navigate("journal") }
            )
        }
        composable("journal") {
            JournalScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalkSelected = { walkId -> navController.navigate("detail/$walkId") }
            )
        }
        composable("detail/{walkId}") { backStackEntry ->
            val walkId = backStackEntry.arguments?.getString("walkId")?.toLongOrNull()
            if (walkId != null) {
                WalkDetailScreen(
                    walkId = walkId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
