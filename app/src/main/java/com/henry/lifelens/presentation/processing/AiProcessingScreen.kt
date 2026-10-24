package com.henry.lifelens.presentation.processing

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val blue = Color(0xFF5865F2)
private val violet = Color(0xFF8B5CF6)
private val cyan = Color(0xFF22D3EE)

/** Hiển thị khi OCR đang chạy; không mô phỏng phần trăm hay tiến độ giả. */
@Composable
fun AiProcessingScreen(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "processing")
    val angle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "orbit"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFE7F3FF), Color(0xFFF3F0FF), Color(0xFFF9FAFF)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("LIFELENS AI", color = blue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 13.sp)
            Spacer(Modifier.height(32.dp))
            Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val base = size.minDimension
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(violet.copy(alpha = .20f), cyan.copy(alpha = .13f), Color.Transparent),
                            center = center, radius = base * .50f
                        ), radius = base * .50f
                    )
                    for (i in 0..2) {
                        drawCircle(
                            color = if (i % 2 == 0) cyan.copy(alpha = .40f) else violet.copy(alpha = .35f),
                            radius = base * (.31f + i * .075f), style = Stroke(width = 2.dp.toPx())
                        )
                    }
                    for (i in 0 until 8) {
                        val a = Math.toRadians((angle + i * 45f).toDouble())
                        val r = base * .43f
                        drawCircle(
                            color = if (i % 2 == 0) cyan else violet,
                            radius = 3.dp.toPx(),
                            center = Offset(center.x + cos(a).toFloat() * r, center.y + sin(a).toFloat() * r)
                        )
                    }
                }
                Box(
                    Modifier.size((142 * pulse).dp)
                        .background(Brush.linearGradient(listOf(cyan, blue, violet)), CircleShape)
                        .padding(6.dp)
                        .background(Brush.radialGradient(listOf(blue, violet)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null,
                        tint = Color.White, modifier = Modifier.size(58.dp))
                }
            }
            Spacer(Modifier.height(26.dp))
            Text("Đang phân tích hình ảnh", color = Color(0xFF18223B),
                fontWeight = FontWeight.Bold, fontSize = 23.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Text("LifeLens đang nhận diện văn bản từ ảnh của bạn.",
                color = Color(0xFF737D92), fontSize = 14.sp, lineHeight = 21.sp,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(color = blue, strokeWidth = 3.dp, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(14.dp))
            Text("Vui lòng chờ trong giây lát", color = blue, fontSize = 13.sp)
        }
    }
}