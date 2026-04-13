package com.mgbheights.android.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mgbheights.android.MainActivity
import com.mgbheights.android.MgbHeightsApp
import com.mgbheights.android.R
import timber.log.Timber

/**
 * Push notification handler.
 * Firebase Messaging has been removed. To re-enable push notifications,
 * integrate a service like OneSignal or re-add FCM without Firebase Auth.
 */
class MgbFcmService {

    fun showLocalNotification(
        context: android.content.Context,
        title: String,
        body: String,
        type: String = "general"
    ) {
        val channelId = when (type) {
            "visitor_approval" -> MgbHeightsApp.CHANNEL_VISITOR
            "payment", "payment_reminder" -> MgbHeightsApp.CHANNEL_PAYMENT
            "emergency" -> MgbHeightsApp.CHANNEL_EMERGENCY
            else -> MgbHeightsApp.CHANNEL_GENERAL
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            Timber.e(e, "Notification permission not granted")
        }
    }
}
