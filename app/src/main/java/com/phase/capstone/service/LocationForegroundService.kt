package com.phase.capstone.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.phase.capstone.R
import com.phase.capstone.main.MainActivity

class LocationForegroundService : Service() {
    private val locationHandler = LocationHandler()

    companion object {
        const val LOCATION_CHANNEL_ID = "DEFAULT_CHANNEL"
        const val TRACKING_CHANNEL_ID = "TRACKING_CHANNEL"
    }

    private fun createLocationNotificationChannel() {
        val notificationChannel = NotificationChannel(
            LOCATION_CHANNEL_ID, "LOCATION LISTENER CHANNEL", NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun createTrackingNotificationChannel(): NotificationManager {
        val notificationChannel = NotificationChannel(
            TRACKING_CHANNEL_ID, "TRACKING CHANNEL", NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        return notificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        createLocationNotificationChannel()
        val notificationManager = createTrackingNotificationChannel()

        val notification: Notification = Notification.Builder(this, LOCATION_CHANNEL_ID)
            .setContentTitle("HomeGroup")
            .setContentText("Current Location is being accessed and used")
            .setSmallIcon(R.drawable.ic_baseline_location)
            .setContentIntent(pendingIntent)
            .build()

        locationHandler.listenerActive = true
        locationHandler.listenCurrentLocation(this)

        locationHandler.listenToGroupLocation(this, TRACKING_CHANNEL_ID, notificationManager)

        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        locationHandler.listenerActive = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}