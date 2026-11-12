package com.pandakind.steptrail

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
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
 * Настраивает навигацию и запрос разрешений.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Запрашиваем разрешения при запуске
        requestPermissionsIfNeeded()

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

    /**
     * Запрашивает разрешения на геолокацию при первом запуске.
     */
    private fun requestPermissionsIfNeeded() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) {
            registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
            ) { }.launch(permissions.toTypedArray())
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
