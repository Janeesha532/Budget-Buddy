package com.example.budgebuddy3.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.budgebuddy3.R
import com.example.budgebuddy3.MainActivity
import java.util.Calendar

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (hasNotificationPermission()) {
            createNotificationChannels()
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Budget alerts channel
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERTS,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for budget alerts"
            }
            
            // Daily reminder channel
            val reminderChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily budget tracking reminders"
            }

            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun showBudgetAlertNotification(title: String, message: String) {
        if (!hasNotificationPermission()) return

        showNotification(CHANNEL_BUDGET_ALERTS, NOTIFICATION_ID_BUDGET, title, message)
    }

    fun showDailyReminder() {
        if (!hasNotificationPermission()) return

        showNotification(
            CHANNEL_DAILY_REMINDER,
            NOTIFICATION_ID_REMINDER,
            "Daily Budget Reminder",
            "Don't forget to track your expenses for today!"
        )
    }

    private fun showNotification(channelId: String, notificationId: Int, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun scheduleDailyReminder(enabled: Boolean) {
        if (!hasNotificationPermission()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (enabled) {
            // Set alarm for 9 PM daily
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 21)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                
                // If it's past 9 PM, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun sendBudgetPushNotification(title: String, message: String) {
        if (!hasNotificationPermission()) return

        // Without Firebase, we'll just show a local notification instead
        showBudgetAlertNotification(title, message)
    }

    companion object {
        private const val CHANNEL_BUDGET_ALERTS = "budget_alerts_channel"
        private const val CHANNEL_DAILY_REMINDER = "daily_reminder_channel"
        private const val NOTIFICATION_ID_BUDGET = 1
        private const val NOTIFICATION_ID_REMINDER = 2
        private const val REMINDER_REQUEST_CODE = 100
    }
}

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper(context).showDailyReminder()
    }
} 