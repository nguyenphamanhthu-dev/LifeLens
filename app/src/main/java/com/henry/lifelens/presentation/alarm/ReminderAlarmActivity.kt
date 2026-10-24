package com.henry.lifelens.presentation.alarm

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessAlarm
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henry.lifelens.notification.AlarmSoundManager
import com.henry.lifelens.ui.theme.LifeLensTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {

            setShowWhenLocked(true)
            setTurnScreenOn(true)

        } else {

            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        val title =
            intent.getStringExtra("title")
                ?: "Nhắc nhở LifeLens"

        val location =
            intent.getStringExtra("location")
                ?: ""

        val time =
            intent.getStringExtra("time")
                ?: SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Date())

        setContent {

            LifeLensTheme {

                ReminderAlarmScreen(
                    title = title,
                    time = time,
                    location = location,
                    onStopAlarm = {

                        AlarmSoundManager.stopAlarm()

                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}


@Composable
private fun ReminderAlarmScreen(
    title: String,
    time: String,
    location: String,
    onStopAlarm: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F8FC)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {


                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFF0F2FF),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.AccessAlarm,
                        contentDescription = null,
                        tint = Color(0xFF4D63F5),
                        modifier = Modifier
                            .height(56.dp)
                    )
                }


                Spacer(
                    modifier = Modifier.height(32.dp)
                )



                Text(
                    text = "LIFELENS",
                    color = Color(0xFF4D63F5),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )


                Spacer(
                    modifier = Modifier.height(16.dp)
                )



                Text(
                    text = time,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )


                Spacer(
                    modifier = Modifier.height(20.dp)
                )



                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )



                if (location.isNotBlank()) {

                    Spacer(
                        modifier = Modifier.height(14.dp)
                    )

                    androidx.compose.foundation.layout.Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF8A94A6)
                        )

                        Text(
                            text = location,
                            modifier =
                                Modifier.padding(
                                    start = 6.dp
                                ),
                            fontSize = 16.sp,
                            color = Color(0xFF8A94A6)
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.height(60.dp)
                )



                Button(
                    onClick = onStopAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape =
                        RoundedCornerShape(18.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF4D63F5)
                        )
                ) {

                    Text(
                        text = "Tắt báo thức",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}