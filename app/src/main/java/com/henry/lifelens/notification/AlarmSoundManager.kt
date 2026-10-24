package com.henry.lifelens.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object AlarmSoundManager {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    fun startAlarm(
        context: Context
    ) {

        // Tránh phát nhiều lần cùng lúc
        if (ringtone?.isPlaying == true) {
            return
        }

        startRingtone(context)

        startVibration(context)
    }

    private fun startRingtone(
        context: Context
    ) {

        try {

            // Ưu tiên âm báo thức mặc định
            var alarmUri =
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
                )

            // Nếu máy không có âm Alarm
            // thì dùng Notification
            if (alarmUri == null) {

                alarmUri =
                    RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION
                    )
            }

            val newRingtone =
                RingtoneManager.getRingtone(
                    context.applicationContext,
                    alarmUri
                )

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.LOLLIPOP
            ) {

                newRingtone.audioAttributes =
                    AudioAttributes.Builder()
                        .setUsage(
                            AudioAttributes.USAGE_ALARM
                        )
                        .setContentType(
                            AudioAttributes.CONTENT_TYPE_SONIFICATION
                        )
                        .build()
            }

            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.P
            ) {

                newRingtone.isLooping = true
            }

            ringtone = newRingtone

            ringtone?.play()

        } catch (exception: Exception) {

            exception.printStackTrace()
        }
    }


    private fun startVibration(
        context: Context
    ) {

        try {

            vibrator =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.S
                ) {

                    val vibratorManager =
                        context.getSystemService(
                            Context.VIBRATOR_MANAGER_SERVICE
                        ) as VibratorManager

                    vibratorManager.defaultVibrator

                } else {

                    @Suppress("DEPRECATION")

                    context.getSystemService(
                        Context.VIBRATOR_SERVICE
                    ) as Vibrator
                }


            // Rung:
            // nghỉ 0ms
            // rung 800ms
            // nghỉ 500ms
            // rung 800ms
            // rồi lặp lại
            val pattern =
                longArrayOf(
                    0,
                    800,
                    500,
                    800
                )


            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        pattern,
                        0
                    )
                )

            } else {

                @Suppress("DEPRECATION")

                vibrator?.vibrate(
                    pattern,
                    0
                )
            }

        } catch (exception: Exception) {

            exception.printStackTrace()
        }
    }


    fun stopAlarm() {

        try {

            ringtone?.stop()

        } catch (exception: Exception) {

            exception.printStackTrace()
        }

        ringtone = null


        try {

            vibrator?.cancel()

        } catch (exception: Exception) {

            exception.printStackTrace()
        }

        vibrator = null
    }
}