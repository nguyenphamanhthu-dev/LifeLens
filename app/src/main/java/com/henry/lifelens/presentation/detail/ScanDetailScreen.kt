package com.henry.lifelens.presentation.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DetailBlue = Color(0xFF5865F2)
private val DetailViolet = Color(0xFF8B5CF6)
private val DetailCyan = Color(0xFF22D3EE)
private val DetailBackground = Color(0xFFF7F8FC)
private val DetailText = Color(0xFF111827)
private val DetailMuted = Color(0xFF7C8496)
private val DetailGradient = Brush.linearGradient(listOf(DetailCyan, DetailBlue, DetailViolet))

@Composable
fun ScanDetailScreen(
    scanId: Long,
    viewModel: ScanDetailViewModel,
    onBackClick: () -> Unit
) {
    val scan by viewModel.scan.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showFullImage by remember { mutableStateOf(false) }

    LaunchedEffect(scanId) {
        viewModel.loadScan(scanId)
    }

    val currentScan = scan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE2EDFF), Color(0xFFF2EEFF), DetailBackground)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 36.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color.White, shadowElevation = 3.dp) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = DetailText)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Chi tiết bản quét", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = DetailText)
                    Text("LifeLens · Scan Detail", fontSize = 12.sp, color = DetailMuted)
                }
                Box(
                    modifier = Modifier
                        .size(43.dp)
                        .clip(CircleShape)
                        .background(DetailGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = Color.White)
                }
            }
            Spacer(Modifier.height(25.dp))

            if (currentScan == null) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = DetailBlue)
                        Spacer(Modifier.height(16.dp))
                        Text("Đang tải nội dung...", color = DetailMuted)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Bản quét đã lưu", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DetailBlue)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatScanTime(currentScan.createdAt),
                        fontSize = 11.sp,
                        color = DetailMuted
                    )
                }

                Spacer(Modifier.height(14.dp))

                if (currentScan.imageUri.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = Uri.parse(currentScan.imageUri),
                                contentDescription = "Ảnh đã lưu, nhấn để xem lớn",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(235.dp)
                                    .clickable { showFullImage = true },
                                contentScale = ContentScale.Crop
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Image, null, tint = DetailViolet, modifier = Modifier.size(19.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Ảnh gốc", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DetailText)
                                Spacer(Modifier.weight(1f))
                                Text("Chạm để phóng to", fontSize = 11.sp, color = DetailMuted)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = DetailBlue, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.width(9.dp))
                    Text("Văn bản nhận diện", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DetailText)
                }
                Spacer(Modifier.height(7.dp))
                Text("Nội dung đã được lưu từ lần quét trước.", fontSize = 13.sp, color = DetailMuted)
                Spacer(Modifier.height(13.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(19.dp)) {
                        Text(
                            currentScan.text.ifBlank { "Không có nội dung văn bản." },
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = DetailText
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(currentScan.text))
                            Toast.makeText(context, "Đã sao chép văn bản", Toast.LENGTH_SHORT).show()
                        },
                        enabled = currentScan.text.isNotBlank(),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DetailBlue)
                    ) {
                        Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Sao chép", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, currentScan.text)
                            }
                            context.startActivity(Intent.createChooser(intent, "Chia sẻ văn bản"))
                        },
                        enabled = currentScan.text.isNotBlank(),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DetailBlue)
                    ) {
                        Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Chia sẻ", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }

    if (showFullImage && currentScan != null && currentScan.imageUri.isNotBlank()) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF101426))
            ) {
                AsyncImage(
                    model = Uri.parse(currentScan.imageUri),
                    contentDescription = "Ảnh phóng to",
                    modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp, max = 600.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { showFullImage = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Close, "Đóng ảnh", tint = Color.White)
                }
            }
        }
    }
}
private fun formatScanTime(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat(
        "dd/MM/yyyy • HH:mm",
        java.util.Locale.getDefault()
    )

    return formatter.format(
        java.util.Date(timestamp)
    )
}