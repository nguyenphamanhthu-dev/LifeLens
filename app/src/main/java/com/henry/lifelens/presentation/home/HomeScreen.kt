package com.henry.lifelens.presentation.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.henry.lifelens.data.local.ReminderEntity
import com.henry.lifelens.data.local.ScanEntity
import com.henry.lifelens.data.ocr.recognizeTextFromImage
import com.henry.lifelens.presentation.processing.AiProcessingScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val LifeLensBlue = Color(0xFF5865F2)
private val LifeLensViolet = Color(0xFF8B5CF6)
private val LifeLensCyan = Color(0xFF22D3EE)
private val LifeLensBackground = Color(0xFFF7F8FC)
private val LifeLensSoftBlue = Color(0xFFF0F2FF)
private val LifeLensText = Color(0xFF111827)
private val LifeLensSecondaryText = Color(0xFF7C8496)
private val LensGradient = Brush.linearGradient(listOf(LifeLensCyan, LifeLensBlue, LifeLensViolet))

@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onGalleryScanCompleted: (Uri, String) -> Unit,
    viewModel: HomeViewModel,
    onHistoryClick: () -> Unit,
    onReminderClick: () -> Unit,
    onMapClick: () -> Unit,
    onLinkClick: () -> Unit,
    onPhoneClick: () -> Unit
) {
    val recentScans by viewModel.recentScans.collectAsState()
    val upcomingReminder by viewModel.upcomingReminder.collectAsState()
    val context = LocalContext.current
    var isAnalyzing by remember { mutableStateOf(false) }
    var showOcrFailed by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isAnalyzing = true
            android.util.Log.d("LifeLensOCR", "START uri=$uri")
            recognizeTextFromImage(
                context = context,
                imageUri = uri,
                onSuccess = { text ->
                    android.util.Log.d("LifeLensOCR", "SUCCESS length=${text.length}")
                    isAnalyzing = false
                    if (text.isBlank()) showOcrFailed = true
                    else onGalleryScanCompleted(uri, text)
                },
                onError = { error ->
                    android.util.Log.e("LifeLensOCR", "ERROR", error)
                    isAnalyzing = false
                    showOcrFailed = true
                    error.printStackTrace()
                }
            )
        }
    }
    val openGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Box(Modifier.fillMaxSize().background(LifeLensBackground)) {
        Box(Modifier.fillMaxWidth().height(370.dp).background(
            Brush.verticalGradient(listOf(Color(0xFFE0EDFF), Color(0xFFF3F1FF), LifeLensBackground))
        ))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 22.dp, bottom = 28.dp)
        ) {
            V3Header()
            Spacer(Modifier.height(10.dp))
            AiOrb(modifier = Modifier.align(Alignment.CenterHorizontally), onClick = onCameraClick)
            Text("Bạn muốn LifeLens\nnhìn gì hôm nay?", Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, fontSize = 25.sp, lineHeight = 31.sp,
                fontWeight = FontWeight.Bold, color = LifeLensText)
            Spacer(Modifier.height(8.dp))
            Text("Quét hình ảnh để AI giúp bạn\nhiểu và hành động nhanh hơn.",
                Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                fontSize = 14.sp, lineHeight = 21.sp, color = LifeLensSecondaryText)
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                V3ScanButton("Quét bằng camera", Icons.Filled.CameraAlt, true,
                    Modifier.weight(1f), onCameraClick)
                V3ScanButton("Chọn từ thư viện", Icons.Filled.Image, false,
                    Modifier.weight(1f), openGallery)
            }
            Spacer(Modifier.height(25.dp))
            Text("✦  KHÁM PHÁ CÙNG LIFELENS", fontSize = 12.sp,
                fontWeight = FontWeight.Bold, color = LifeLensBlue)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                V3Feature(Icons.Filled.CalendarMonth, "Nhắc nhở", "Tự nhận diện", onReminderClick)
                V3Feature(Icons.Filled.LocationOn, "Bản đồ", "Tìm địa điểm", onMapClick)
                V3Feature(Icons.Filled.Link, "Liên kết", "Mở website", onLinkClick)
                V3Feature(Icons.Filled.Phone, "Điện thoại", "Gọi nhanh", onPhoneClick)
            }
            Spacer(Modifier.height(28.dp))
            upcomingReminder?.let { reminder ->
                V3SectionTitle("✦  Nhắc nhở sắp tới")
                Spacer(Modifier.height(12.dp))
                V3Reminder(reminder)
                Spacer(Modifier.height(25.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { V3SectionTitle("Quét gần đây") }
                TextButton(onClick = onHistoryClick) { Text("Xem tất cả  ›", color = LifeLensBlue) }
            }
            Spacer(Modifier.height(8.dp))
            if (recentScans.isEmpty()) {
                V3Surface { Text("Chưa có nội dung nào. Hãy thử quét ảnh đầu tiên!",
                    color = LifeLensSecondaryText, fontSize = 13.sp) }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    recentScans.take(2).forEach { scan ->
                        V3RecentScan(scan, Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        if (isAnalyzing) AiProcessingScreen()
        if (showOcrFailed) OcrFailedOverlay(
            onChooseAnotherImage = { showOcrFailed = false; openGallery() },
            onDismiss = { showOcrFailed = false }
        )
    }
}
@Composable
private fun V3Header() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(43.dp).clip(CircleShape).background(LensGradient),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("LifeLens", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = LifeLensText)
            Text("Smart Visual Assistant", fontSize = 11.sp, color = LifeLensSecondaryText)
        }
        Surface(shape = CircleShape, color = Color.White, shadowElevation = 4.dp) {
            Icon(Icons.Filled.NotificationsNone, "Nhắc nhở", tint = LifeLensBlue,
                modifier = Modifier.padding(12.dp).size(23.dp))
        }
    }
}

@Composable
private fun AiOrb(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "AI Orb")
    val rotation by infinite.animateFloat(0f, 360f,
        infiniteRepeatable(tween(16000, easing = LinearEasing)), label = "Orbit")
    val pulse by infinite.animateFloat(0.96f, 1.05f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Pulse")
    val glow by infinite.animateFloat(0.28f, 0.58f,
        infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "Glow")
    Box(modifier.size(270.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val base = size.minDimension
            drawCircle(brush = Brush.radialGradient(listOf(
                LifeLensViolet.copy(alpha = glow * .45f),
                LifeLensCyan.copy(alpha = glow * .35f), Color.Transparent
            ), center = center, radius = base * .5f), radius = base * .5f)
            for (i in 0..3) {
                drawCircle(
                    color = if (i % 2 == 0) LifeLensCyan.copy(alpha = .28f)
                    else LifeLensViolet.copy(alpha = .24f),
                    radius = base * (.31f + i * .045f),
                    style = Stroke(width = if (i == 1) 3.dp.toPx() else 1.2.dp.toPx())
                )
            }
            for (i in 0 until 9) {
                val a = Math.toRadians((rotation + i * 40f).toDouble())
                val r = base * (if (i % 3 == 0) .43f else .36f)
                drawCircle(
                    color = if (i % 2 == 0) LifeLensCyan else Color.White,
                    radius = (if (i % 3 == 0) 4f else 2.5f).dp.toPx(),
                    center = Offset(center.x + cos(a).toFloat() * r,
                        center.y + sin(a).toFloat() * r)
                )
            }
        }
        Box(Modifier.size((154 * pulse).dp).shadow(22.dp, CircleShape)
            .clip(CircleShape).background(LensGradient).padding(7.dp)
            .clip(CircleShape).background(Brush.radialGradient(listOf(
                Color(0xFF245DFF), Color(0xFF3B40DC), Color(0xFF7129D8)
            ))), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AutoAwesome, "AI Lens - chạm để mở camera",
                tint = Color.White, modifier = Modifier.size(56.dp))
        }
    }
}

@Composable
private fun V3ScanButton(
    title: String, icon: ImageVector, primary: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Box(modifier.height(110.dp).clip(RoundedCornerShape(22.dp))
        .background(if (primary) LensGradient else Brush.linearGradient(
            listOf(Color.White, Color(0xFFF7F7FF))))
        .clickable(onClick = onClick).padding(14.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = if (primary) Color.White else LifeLensViolet,
                modifier = Modifier.size(29.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp,
                color = if (primary) Color.White else LifeLensText)
        }
    }
}
@Composable
private fun V3Feature(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(77.dp).clickable(onClick = onClick)) {
        Box(Modifier.size(54.dp).shadow(5.dp, RoundedCornerShape(17.dp))
            .clip(RoundedCornerShape(17.dp)).background(Color.White),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = LifeLensBlue, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LifeLensText,
            maxLines = 1)
        Text(subtitle, fontSize = 9.sp, color = LifeLensSecondaryText, maxLines = 1)
    }
}

@Composable
private fun V3SectionTitle(title: String) {
    Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = LifeLensText)
}

@Composable
private fun V3Surface(content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White,
        shadowElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.padding(17.dp)) { content() }
    }
}

@Composable
private fun V3Reminder(reminder: ReminderEntity) {
    V3Surface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.size(width = 70.dp, height = 76.dp)
                .clip(RoundedCornerShape(15.dp)).background(LifeLensSoftBlue),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                val parts = reminder.date.split("/")
                Text(parts.getOrNull(0) ?: "--", color = LifeLensBlue,
                    fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text(parts.getOrNull(1)?.let { "THÁNG $it" } ?: "", fontSize = 10.sp,
                    color = LifeLensBlue)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(reminder.title, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = LifeLensText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text("◷  ${reminder.time} • ${reminder.date}", fontSize = 12.sp,
                    color = LifeLensSecondaryText)
                if (reminder.location.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text("⌖  ${reminder.location}", fontSize = 12.sp,
                        color = LifeLensSecondaryText, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Filled.NotificationsActive, null, tint = LifeLensViolet,
                modifier = Modifier.size(21.dp))
        }
    }
}
@Composable
private fun V3RecentScan(scan: ScanEntity, modifier: Modifier = Modifier) {
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(16.dp))
            .background(LifeLensSoftBlue), contentAlignment = Alignment.Center) {
            if (scan.imageUri.isNotBlank()) {
                AsyncImage(model = Uri.parse(scan.imageUri), contentDescription = "Ảnh đã quét",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else Icon(Icons.Filled.Image, null, tint = LifeLensBlue)
        }
        Spacer(Modifier.height(7.dp))
        Text(scan.text.lineSequence().firstOrNull { it.isNotBlank() } ?: "Bản quét",
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LifeLensText,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(scan.createdAt)),
            fontSize = 10.sp, color = LifeLensSecondaryText)
    }
}

@Composable
private fun AnalyzingOverlay() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.55f
                )
            ),

        contentAlignment = Alignment.Center
    ) {

        Card(
            shape = RoundedCornerShape(20.dp),

            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 32.dp,
                    vertical = 28.dp
                ),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CircularProgressIndicator(
                    modifier = Modifier.size(42.dp),
                    color = LifeLensBlue,
                    strokeWidth = 3.dp
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Text(
                    text = "Đang phân tích hình ảnh...",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = LifeLensText
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Có thể mất vài giây.",
                    fontSize = 13.sp,
                    color = LifeLensSecondaryText
                )
            }
        }
    }
}


@Composable
private fun OcrFailedOverlay(
    onChooseAnotherImage: () -> Unit,
    onDismiss: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = 0.55f
                )
            ),

        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),

            shape = RoundedCornerShape(24.dp),

            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),

            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = LifeLensSoftBlue
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null,
                            tint = LifeLensBlue,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Text(
                    text = "Không tìm thấy văn bản",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LifeLensText
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Hãy thử ảnh rõ hơn hoặc đảm bảo hình ảnh có chứa văn bản.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = LifeLensSecondaryText,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Button(
                    onClick = onChooseAnotherImage,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),

                    shape = RoundedCornerShape(14.dp)
                ) {

                    Text(
                        text = "Chọn ảnh khác",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                TextButton(
                    onClick = onDismiss
                ) {

                    Text(
                        text = "Hủy",
                        color = LifeLensSecondaryText
                    )
                }
            }
        }
    }
}