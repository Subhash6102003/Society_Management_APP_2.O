package com.mgbheights.android.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mgbheights.android.MainActivity
import com.mgbheights.android.MgbHeightsApp
import com.mgbheights.android.R
import com.mgbheights.shared.domain.repository.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MgbFcmService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                authRepository.updateFcmToken(token)
            } catch (e: Exception) {
                Timber.e(e, "Failed to update FCM token")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: ${message.data}")

        val data = message.data
        val type = data["type"] ?: "general"
        val title = data["title"] ?: message.notification?.title ?: "MGB Heights"
        val body = data["body"] ?: message.notification?.body ?: ""

        val channelId = when (type) {
            "visitor_approval" -> MgbHeightsApp.CHANNEL_VISITOR
            "payment", "payment_reminder", "payment_due" -> MgbHeightsApp.CHANNEL_PAYMENT
            "emergency" -> MgbHeightsApp.CHANNEL_EMERGENCY
            else -> MgbHeightsApp.CHANNEL_GENERAL
        }

        showNotification(title, body, channelId, data)
    }

    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (channelId == MgbHeightsApp.CHANNEL_EMERGENCY)
                    NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .build()

        try {
            NotificationManagerCompat.from(this).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        } catch (e: SecurityException) {
            Timber.e(e, "Notification permission not granted")
        }
    }
}

