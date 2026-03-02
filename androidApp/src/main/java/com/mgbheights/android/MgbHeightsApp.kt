package com.mgbheights.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MgbHeightsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // General notifications
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications"
            }

            // Visitor approvals
            val visitorChannel = NotificationChannel(
                CHANNEL_VISITOR,
                "Visitor Approvals",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Visitor approval requests"
                enableVibration(true)
            }

            // Payment notifications
            val paymentChannel = NotificationChannel(
                CHANNEL_PAYMENT,
                "Payments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Payment notifications and reminders"
            }

            // Emergency alerts
            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency broadcasts"
                enableVibration(true)
                enableLights(true)
            }

            manager.createNotificationChannels(
                listOf(generalChannel, visitorChannel, paymentChannel, emergencyChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_GENERAL = "mgb_heights_general"
        const val CHANNEL_VISITOR = "mgb_heights_visitor"
        const val CHANNEL_PAYMENT = "mgb_heights_payment"
        const val CHANNEL_EMERGENCY = "mgb_heights_emergency"
    }
}

