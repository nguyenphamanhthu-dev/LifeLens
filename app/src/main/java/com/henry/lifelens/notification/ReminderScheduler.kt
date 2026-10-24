package com.henry.lifelens.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Locale

class ReminderScheduler(
    private val context: Context
) {

    // ========================================================
    // SCHEDULE REMINDER
    // ========================================================

    fun scheduleReminder(
        reminderId: Long,
        title: String,
        time: String,
        location: String,
        triggerAtMillis: Long
    ): Boolean {
        android.util.Log.e(
            "LifeLensDebug",
            "3 - ĐÃ VÀO scheduleReminder: trigger=$triggerAtMillis"
        )

        // Không cho đặt thời gian trong quá khứ
        if (triggerAtMillis <= System.currentTimeMillis()) {
            return false
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            android.util.Log.e(
                "LifeLensDebug",
                "EXACT ALARM PERMISSION = ${alarmManager.canScheduleExactAlarms()}"
            )

            if (!alarmManager.canScheduleExactAlarms()) {

                android.util.Log.e(
                    "LifeLensDebug",
                    "DỪNG: Samsung chưa cho phép Exact Alarm"
                )

                return false
            }
        }

        val intent =
            Intent(
                context,
                ReminderReceiver::class.java
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
                    "time",
                    time
                )

                putExtra(
                    "location",
                    location
                )

                putExtra(
                    "triggerAtMillis",
                    triggerAtMillis
                )
            }


        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminderId.toRequestCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )



        return try {
            val debugFormat =
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm:ss",
                    Locale.getDefault()
                )

            android.util.Log.d(
                "LifeLensAlarm",
                "NOW = ${
                    debugFormat.format(
                        java.util.Date(
                            System.currentTimeMillis()
                        )
                    )
                }"
            )

            android.util.Log.d(
                "LifeLensAlarm",
                "ALARM = ${
                    debugFormat.format(
                        java.util.Date(
                            triggerAtMillis
                        )
                    )
                }"
            )

            android.util.Log.d(
                "LifeLensAlarm",
                "DIFF = ${
                    triggerAtMillis -
                            System.currentTimeMillis()
                } ms"
            )

            android.util.Log.d(
                "LifeLensAlarm",
                "ID = $reminderId"
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            android.util.Log.e(
                "LifeLensDebug",
                "4 - ALARM ĐÃ ĐƯỢC ĐẶT"
            )
            true

        } catch (exception: Exception) {

            exception.printStackTrace()

            false
        }
    }


    fun cancelReminder(
        reminderId: Long
    ) {

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        val intent =
            Intent(
                context,
                ReminderReceiver::class.java
            )

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminderId.toRequestCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(
            pendingIntent
        )

        pendingIntent.cancel()
    }


    fun canScheduleExactAlarms(): Boolean {

        // Android dưới 12 không cần kiểm tra
        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.S
        ) {
            return true
        }

        val alarmManager =
            context.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        return alarmManager
            .canScheduleExactAlarms()
    }


    fun openExactAlarmSettings() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {

            try {

                val intent =
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    ).apply {

                        data =
                            Uri.parse(
                                "package:${context.packageName}"
                            )

                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                context.startActivity(
                    intent
                )

            } catch (exception: Exception) {

                exception.printStackTrace()
            }
        }
    }



    fun parseDateTime(
        date: String,
        time: String
    ): Long? {

        return try {

            val normalizedDate =
                date
                    .trim()
                    .replace(".", "/")
                    .replace("-", "/")


            val normalizedTime =
                time
                    .trim()
                    .replace("h", ":")
                    .replace("H", ":")


            val formatter =
                SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                ).apply {

                    isLenient = false
                }


            formatter
                .parse(
                    "$normalizedDate $normalizedTime"
                )
                ?.time

        } catch (exception: Exception) {

            exception.printStackTrace()

            null
        }
    }



    private fun Long.toRequestCode(): Int {

        return (
                this xor
                        (this ushr 32)
                ).toInt()
    }
}