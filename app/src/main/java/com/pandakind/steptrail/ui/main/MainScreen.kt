package com.pandakind.steptrail.ui.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pandakind.steptrail.ui.theme.*

/**
 * Главный экран приложения.
 * Отображает статистику прогулки и кнопку Старт/Стоп.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateToJournal: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Launcher для запроса разрешений
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineLocationGranted && coarseLocationGranted) {
            // Разрешения получены
        }
    }

    // Проверяем, есть ли разрешения
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToJournal) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = stringResource(R.string.journal_distance)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Индикатор статуса GPS
            GpsStatusIndicator(status = uiState.gpsStatus)

            // Кнопка Старт/Стоп
            TrackingButton(
                isTracking = uiState.isTracking,
                onClick = viewModel::toggleTracking
            )

            // Статистика
            StatisticsGrid(uiState = uiState)
        }
    }
}

/**
 * Индикатор статуса GPS.
 */
@Composable
private fun GpsStatusIndicator(status: GpsStatus) {
    val info = when (status) {
        GpsStatus.NO_PERMISSION -> Triple(
            stringResource(R.string.gps_no_permission),
            Red,
            Icons.Default.Close
        )
        GpsStatus.SEARCHING -> Triple(
            stringResource(R.string.gps_searching),
            Orange,
            Icons.Default.Refresh
        )
        GpsStatus.ACTIVE -> Triple(
            stringResource(R.string.gps_active),
            Green,
            Icons.Default.LocationOn
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = info.second.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = info.third,
                contentDescription = null,
                tint = info.second,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = info.first,
                color = info.second,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Большая кнопка Старт/Стоп.
 */
@Composable
private fun TrackingButton(isTracking: Boolean, onClick: () -> Unit) {
    val (text, bgColor) = if (isTracking) {
        stringResource(R.string.btn_stop) to Red
    } else {
        stringResource(R.string.btn_start) to Green
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor)
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Сетка статистики.
 */
@Composable
private fun StatisticsGrid(uiState: MainUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatRow(
            label = stringResource(R.string.speed_current),
            value = "%.1f".format(uiState.currentSpeedKmh),
            unit = stringResource(R.string.kmh)
        )
        StatRow(
            label = stringResource(R.string.speed_avg),
            value = "%.1f".format(uiState.avgSpeedKmh),
            unit = stringResource(R.string.kmh)
        )
        StatRow(
            label = stringResource(R.string.distance),
            value = "%.2f".format(uiState.distanceKm),
            unit = stringResource(R.string.km)
        )
        StatRow(
            label = stringResource(R.string.duration),
            value = formatDuration(uiState.durationSeconds),
            unit = ""
        )
    }
}

/**
 * Строка статистики.
 */
@Composable
private fun StatRow(label: String, value: String, unit: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Text(
                text = "$value $unit",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

/**
 * Форматирует длительность в секундах в строку чч:мм:сс.
 */
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%02d:%02d".format(minutes, secs)
    }
}
