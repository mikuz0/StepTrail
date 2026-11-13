package com.pandakind.steptrail.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pandakind.steptrail.R
import com.pandakind.steptrail.ui.main.formatDuration
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Экран деталей прогулки.
 * Отображает полную информацию о завершённой прогулке.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalkDetailScreen(
    walkId: Long,
    onNavigateBack: () -> Unit,
    viewModel: WalkDetailViewModel = hiltViewModel()
) {
    val walk by viewModel.walk.collectAsStateWithLifecycle()

    LaunchedEffect(walkId) {
        viewModel.loadWalk(walkId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        walk?.let { w ->
            WalkDetailContent(
                walk = w,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        } ?: run {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * Контент экрана деталей прогулки.
 */
@Composable
private fun WalkDetailContent(walk: com.pandakind.steptrail.domain.model.Walk, modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Дата
        DetailCard(
            label = stringResource(R.string.journal_date),
            value = formatDate(walk.startDate)
        )

        // Длительность
        DetailCard(
            label = stringResource(R.string.journal_duration),
            value = formatDuration(walk.durationSeconds)
        )

        // Дистанция
        DetailCard(
            label = stringResource(R.string.journal_distance),
            value = "%.2f %s".format(walk.distanceMeters / 1000.0, stringResource(R.string.km))
        )

        // Средняя скорость
        DetailCard(
            label = stringResource(R.string.journal_avg_speed),
            value = "%.1f %s".format(walk.avgSpeedMps * 3.6, stringResource(R.string.kmh))
        )

        // Максимальная скорость
        DetailCard(
            label = stringResource(R.string.journal_max_speed),
            value = "%.1f %s".format(walk.maxSpeedMps * 3.6, stringResource(R.string.kmh))
        )
    }
}

/**
 * Карточка с детальным значением.
 */
@Composable
private fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

/**
 * Форматирует дату в читаемый вид.
 */
private fun formatDate(date: java.util.Date): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}
