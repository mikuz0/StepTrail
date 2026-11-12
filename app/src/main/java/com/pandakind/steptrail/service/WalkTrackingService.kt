package com.pandakind.steptrail.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pandakind.steptrail.R
import com.pandakind.steptrail.MainActivity
import com.pandakind.steptrail.data.location.LocationTracker
import com.pandakind.steptrail.data.repository.WalkRepositoryImpl
import com.pandakind.steptrail.data.local.AppDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Foreground Service для трекинга прогулок.
 * Запускается при нажатии «Старт», останавливается при «Стоп».
 */
@AndroidEntryPoint
class WalkTrackingService : Service() {

    private val binder = WalkTrackingBinder()
    private var locationTracker: LocationTracker? = null
    private var trackingJob: Job? = null
    private var currentWalkId: Long = 0L

    private var repository: WalkRepositoryImpl? = null

    @Inject
    lateinit var database: AppDatabase

    private val LOCATION_CHANNEL_ID = "steptrail_location_channel"
    private val NOTIFICATION_ID = 1

    /**
     * Binder для связи сервиса с активностью.
     */
    inner class WalkTrackingBinder : Binder() {
        fun getService(): WalkTrackingService = this@WalkTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        // Инициализируем repository вручную, т.к. Hilt может не внедрить в сервис
        repository = WalkRepositoryImpl(database.walkDao())
        locationTracker = LocationTracker(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val walkId = intent.getLongExtra(EXTRA_WALK_ID, 0L)
                currentWalkId = walkId
                startTracking()
            }
            ACTION_STOP -> {
                stopTracking()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    /**
     * Запускает трекинг: foreground notification + GPS updates.
     */
    private fun startTracking() {
        createNotificationChannel()
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        locationTracker?.requestLocationUpdates()

        trackingJob = CoroutineScope(Dispatchers.IO).launch {
            locationTracker?.trackPointFlow?.let { flow ->
                flow.collect { trackPoint ->
                    val tp = trackPoint.copy(walkId = currentWalkId)
                    repository?.saveTrackPoint(tp)
                }
            }
        }
    }

    /**
     * Останавливает трекинг.
     */
    private fun stopTracking() {
        trackingJob?.cancel()
        locationTracker?.removeLocationUpdates()
        CoroutineScope(Dispatchers.IO).launch {
            repository?.stopWalk(currentWalkId)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Создаёт канал уведомлений.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            LOCATION_CHANNEL_ID,
            "Геолокация",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Отслеживание прогулок"
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Строит уведомление foreground service.
     */
    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, LOCATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "action_start_tracking"
        const val ACTION_STOP = "action_stop_tracking"
        const val EXTRA_WALK_ID = "extra_walk_id"
    }
}
