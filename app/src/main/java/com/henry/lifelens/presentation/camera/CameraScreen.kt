package com.henry.lifelens.presentation.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.henry.lifelens.data.ocr.recognizeTextFromImage
import com.henry.lifelens.presentation.processing.AiProcessingScreen
import java.io.File

private val Blue = Color(0xFF5865F2)
private val Violet = Color(0xFF9366FF)
private val Cyan = Color(0xFF29DDF5)
private val Night = Color(0xFF080D26)
private val Glow = Brush.linearGradient(listOf(Cyan, Blue, Violet))

@Composable
fun CameraScreen(
    onScanCompleted: (Uri, String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { if (previous != null) controller.isAppearanceLightStatusBars = previous }
    }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var analyzing by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    Box(Modifier.fillMaxSize().background(Night)) {
        if (hasPermission) {
            LiveScanner(
                onBackClick = onBackClick,
                onScanCompleted = onScanCompleted,
                onAnalyzing = { analyzing = it },
                onFailed = { failed = true }
            )
        } else {
            PermissionView(
                onAllow = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onBack = onBackClick
            )
        }
        if (analyzing) AiProcessingScreen()
        if (failed) FailureView(
            onRetry = { failed = false },
            onBack = { failed = false; onBackClick() }
        )
    }
}

@Composable
private fun LiveScanner(
    onBackClick: () -> Unit,
    onScanCompleted: (Uri, String) -> Unit,
    onAnalyzing: (Boolean) -> Unit,
    onFailed: () -> Unit
) {val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val imageCapture = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var flashOn by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    var cameraReady by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            try {
                val provider = future.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                provider.unbindAll()
                camera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                cameraReady = true
            } catch (e: Exception) {
                e.printStackTrace()
                cameraReady = false
            }
        }
        future.addListener(listener, ContextCompat.getMainExecutor(context))
        onDispose {
            camera?.cameraControl?.enableTorch(false)
            if (future.isDone) try { future.get().unbindAll() } catch (_: Exception) { }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxWidth().height(126.dp).align(Alignment.TopCenter)
            .background(Brush.verticalGradient(listOf(Night.copy(alpha = .94f), Night.copy(alpha = .0f)))))
        Box(Modifier.fillMaxWidth().height(245.dp).align(Alignment.BottomCenter)
            .background(Brush.verticalGradient(listOf(Night.copy(alpha = .0f), Night.copy(alpha = .95f)))))

        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, enabled = !capturing,
                    modifier = Modifier.background(Color.White.copy(alpha = .13f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Color.White)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LIFELENS SCANNER", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp)
                    Text("AI VISION", color = Cyan, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                IconButton(onClick = {
                    val next = !flashOn
                    if (camera?.cameraInfo?.hasFlashUnit() == true) {
                        camera?.cameraControl?.enableTorch(next)
                        flashOn = next
                    }
                }, modifier = Modifier.background(Color.White.copy(alpha = .13f), CircleShape)) {
                    Icon(if (flashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        "Bật hoặc tắt đèn flash", tint = if (flashOn) Cyan else Color.White)
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            val compact = maxHeight < 640.dp
            val frameHeight = if (compact) 215.dp else 265.dp
            val scanOffset = if (compact) (-48).dp else (-45).dp
            val shutterSize = if (compact) 76.dp else 84.dp
            val sideSize = if (compact) 44.dp else 48.dp

            Column(
                modifier = Modifier.align(Alignment.Center).offset(y = scanOffset)
                    .fillMaxWidth().padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Night.copy(alpha = .70f), RoundedCornerShape(30.dp))
                        .border(1.dp, Cyan.copy(alpha = .45f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = Cyan, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("AI SCAN ĐÃ SẴN SÀNG", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(if (compact) 12.dp else 16.dp))
                ScannerFrame(Modifier.fillMaxWidth().height(frameHeight))
                Spacer(Modifier.height(14.dp))
                Text("Đặt văn bản vào trong khung", color = Color.White,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text("Giữ điện thoại ổn định để nhận diện chính xác",
                    color = Color.White.copy(alpha = .8f), fontSize = 11.sp, textAlign = TextAlign.Center)
            }

            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                    .padding(bottom = if (compact) 10.dp else 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hai biểu tượng hai bên chỉ trang trí, không giả làm nút chức năng.
                    Box(Modifier.size(sideSize).background(Color.White.copy(alpha = .12f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.DocumentScanner, null, tint = Cyan, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(32.dp))
                    Box(Modifier.size(shutterSize).background(Glow, CircleShape).padding(4.dp)
                        .background(Night, CircleShape).padding(5.dp)
                        .background(Glow, CircleShape)
                        .clickable(enabled = cameraReady && !capturing) {
                            capturing = true
                            captureAndRecognize(imageCapture, context,
                                onAnalyzing = onAnalyzing,
                                onSuccess = { uri, text ->
                                    capturing = false
                                    if (text.isBlank()) onFailed() else onScanCompleted(uri, text)
                                },
                                onError = { capturing = false; onFailed() })
                        }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CameraAlt, "Chụp và quét văn bản", tint = Color.White,
                            modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(32.dp))
                    Box(Modifier.size(sideSize).background(Color.White.copy(alpha = .12f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = Cyan, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.height(9.dp))
                Text(if (capturing) "Đang chụp..." else "CHẠM ĐỂ QUÉT",
                    color = Color.White.copy(alpha = .88f), fontSize = 11.sp,
                    letterSpacing = 1.8.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScannerFrame(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "scan")
    val scan by infinite.animateFloat(initialValue = .05f, targetValue = .95f,
        animationSpec = infiniteRepeatable(tween(2300, easing = LinearEasing), RepeatMode.Reverse), label = "line")
    val glow by infinite.animateFloat(initialValue = .55f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "glow")
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val corner = 48.dp.toPx()
        val stroke = 4.dp.toPx()
        val inset = 3.dp.toPx()
        val c = Cyan.copy(alpha = glow)
        val p = Violet.copy(alpha = glow)
        drawRoundRect(color = Color.White.copy(alpha = .07f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()))
        drawRoundRect(brush = Brush.linearGradient(listOf(Cyan.copy(alpha = .35f), Violet.copy(alpha = .35f))),
            style = Stroke(width = 1.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()))
        // Four glowing corners
        drawLine(c, Offset(inset, corner), Offset(inset, inset + 18.dp.toPx()), stroke, cap = StrokeCap.Round)
        drawLine(c, Offset(inset, inset), Offset(corner, inset), stroke, cap = StrokeCap.Round)
        drawLine(p, Offset(w - corner, inset), Offset(w - inset, inset), stroke, cap = StrokeCap.Round)
        drawLine(p, Offset(w - inset, inset), Offset(w - inset, corner), stroke, cap = StrokeCap.Round)
        drawLine(c, Offset(inset, h - corner), Offset(inset, h - inset), stroke, cap = StrokeCap.Round)
        drawLine(c, Offset(inset, h - inset), Offset(corner, h - inset), stroke, cap = StrokeCap.Round)
        drawLine(p, Offset(w - corner, h - inset), Offset(w - inset, h - inset), stroke, cap = StrokeCap.Round)
        drawLine(p, Offset(w - inset, h - corner), Offset(w - inset, h - inset), stroke, cap = StrokeCap.Round)
        val y = h * scan
        drawLine(brush = Brush.horizontalGradient(listOf(Color.Transparent, Cyan.copy(alpha = .85f), Violet.copy(alpha = .85f), Color.Transparent)),
            start = Offset(12.dp.toPx(), y), end = Offset(w - 12.dp.toPx(), y), strokeWidth = 3.dp.toPx())
        drawRect(brush = Brush.verticalGradient(listOf(Cyan.copy(alpha = .10f), Color.Transparent),
            startY = y, endY = (y + 42.dp.toPx()).coerceAtMost(h)),
            topLeft = Offset(12.dp.toPx(), y), size = androidx.compose.ui.geometry.Size(w - 24.dp.toPx(), (42.dp.toPx()).coerceAtMost(h - y)))
    }
}
private fun captureAndRecognize(
    imageCapture: ImageCapture,
    context: Context,
    onAnalyzing: (Boolean) -> Unit,
    onSuccess: (Uri, String) -> Unit,
    onError: (Exception) -> Unit
) {
    val photoFile = File(context.cacheDir, "lifelens_${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(options, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val uri = Uri.fromFile(photoFile)
                onAnalyzing(true)
                recognizeTextFromImage(context = context, imageUri = uri,
                    onSuccess = { text -> onAnalyzing(false); onSuccess(uri, text) },
                    onError = { error -> onAnalyzing(false); onError(error) })
            }
            override fun onError(exception: ImageCaptureException) {
                onAnalyzing(false)
                onError(exception)
            }
        })
}

@Composable
private fun AnalyzingView() {
    Box(Modifier.fillMaxSize().background(Night.copy(alpha = .94f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(110.dp).background(Glow, CircleShape).padding(2.dp)
                .background(Night, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Cyan, modifier = Modifier.size(46.dp))
                CircularProgressIndicator(modifier = Modifier.size(100.dp), color = Violet, strokeWidth = 3.dp)
            }
            Spacer(Modifier.height(30.dp))
            Text("LifeLens đang phân tích...", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Đang nhận diện văn bản từ hình ảnh", color = Color.White.copy(alpha = .7f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun FailureView(onRetry: () -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Night.copy(alpha = .84f)).padding(24.dp),
        contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.DocumentScanner, null, tint = Blue, modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(18.dp))
                Text("Không tìm thấy văn bản", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Night)
                Spacer(Modifier.height(10.dp))
                Text("Hãy giữ camera ổn định và đảm bảo văn bản đủ rõ.",
                    color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Text("Quét lại") }
                TextButton(onClick = onBack) { Text("Quay lại", color = Blue) }
            }
        }
    }
}

@Composable
private fun PermissionView(onAllow: () -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFFF7F8FC))) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(10.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Night)
        }
        Column(Modifier.align(Alignment.Center).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(86.dp).background(Glow, RoundedCornerShape(26.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.CameraAlt, null, tint = Color.White, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("Cho phép truy cập Camera", color = Night, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("LifeLens cần quyền sử dụng camera để chụp và nhận diện văn bản.",
                color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
            Spacer(Modifier.height(26.dp))
            Button(onClick = onAllow, modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)) { Text("Cho phép Camera") }
        }
    }
}