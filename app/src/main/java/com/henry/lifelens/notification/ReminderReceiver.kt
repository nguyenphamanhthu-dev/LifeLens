package com.henry.lifelens.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.henry.lifelens.R
import com.henry.lifelens.presentation.alarm.ReminderAlarmActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val reminderId =
            intent.getLongExtra(
                "reminderId",
                -1L
            )

        val title =
            intent.getStringExtra("title")
                ?: "Nhắc nhở LifeLens"

        val location =
            intent.getStringExtra("location")
                ?: ""

        val time =
            intent.getStringExtra("time")
                ?: ""


        AlarmSoundManager.startAlarm(context)


        val alarmIntent =
            Intent(
                context,
                ReminderAlarmActivity::class.java
            ).apply {

                putExtra(
                    "reminderId",
                    reminderId
                )

                putExtra(
                    "title",
                    title
                )

                putExtra(
                    "location",
                    location
                )

                putExtra(
                    "time",
                    time
                )

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val fullScreenPendingIntent =
            PendingIntent.getActivity(
                context,
                reminderId.toRequestCode(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )


        createAlarmChannel(context)


        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(
                    if (location.isNotBlank()) {
                        "$time • $location"
                    } else {
                        time
                    }
                )
                .setPriority(
                    NotificationCompat.PRIORITY_MAX
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(
                    fullScreenPendingIntent,
                    true
                )
                .setContentIntent(
                    fullScreenPendingIntent
                )
                .build()

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.notify(
            reminderId.toNotificationId(),
            notification
        )
    }

    private fun createAlarmChannel(
        context: Context
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Báo thức LifeLens",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {

                    description =
                        "Thông báo báo thức và nhắc nhở LifeLens"

                    lockscreenVisibility =
                        android.app.Notification.VISIBILITY_PUBLIC

                    // Âm thanh đang do AlarmSoundManager xử lý
                    setSound(null, null)

                    enableVibration(false)
                }

            val manager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun Long.toRequestCode(): Int {
        return (
                this xor
                        (this ushr 32)
                ).toInt()
    }

    private fun Long.toNotificationId(): Int {
        return (
                this xor
                        (this ushr 32)
                ).toInt()
    }

    companion object {

        private const val CHANNEL_ID =
            "lifelens_alarm_channel"
    }
}