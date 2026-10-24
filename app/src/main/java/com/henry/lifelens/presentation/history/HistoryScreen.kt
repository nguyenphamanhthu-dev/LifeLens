package com.henry.lifelens.presentation.history

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.henry.lifelens.data.local.ScanEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Blue = Color(0xFF5865F2)
private val Violet = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF22D3EE)
private val Background = Color(0xFFF7F8FC)
private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF7C8496)
private val Accent = Brush.linearGradient(listOf(Cyan, Blue, Violet))

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onScanClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val scans by viewModel.scans.collectAsState()
    var query by remember { mutableStateOf("") }
    var newestFirst by remember { mutableStateOf(true) }
    var scanToDelete by remember { mutableStateOf<ScanEntity?>(null) }

    val filtered = remember(scans, query, newestFirst) {
        val matching = scans.filter { it.text.contains(query.trim(), ignoreCase = true) }
        if (newestFirst) matching.sortedByDescending { it.createdAt }
        else matching.sortedBy { it.createdAt }
    }

    Box(Modifier.fillMaxSize().background(Background)) {
        Box(
            Modifier.fillMaxWidth().height(270.dp).background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE4EEFF), Color(0xFFF1EDFF), Background)
                )
            )
        )
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 20.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = Ink)
                }
                Column(Modifier.weight(1f)) {
                    Text("Lịch sử quét", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Text("Những điều LifeLens đã khám phá", fontSize = 12.sp, color = Muted)
                }
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = Color.White)
                }
            }

            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(23.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Tìm trong nội dung đã quét...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Blue) },
                    trailingIcon = {
                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, "Xóa tìm kiếm")
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Violet,
                        unfocusedBorderColor = Color(0xFFE5E7F0)
                    )
                )
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Bản quét của bạn", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Ink)
                        Text("${filtered.size} bản quét", fontSize = 12.sp, color = Muted)
                    }
                    TextButton(onClick = { newestFirst = !newestFirst }) {
                        Icon(Icons.Filled.SwapVert, null, modifier = Modifier.size(18.dp), tint = Blue)
                        Spacer(Modifier.width(4.dp))
                        Text(if (newestFirst) "Mới nhất" else "Cũ nhất", color = Blue, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            if (scans.isEmpty()) {
                HistoryEmptyState(
                    title = "Chưa có bản quét nào",
                    subtitle = "Hãy quét một hình ảnh và lưu kết quả.\nNội dung sẽ xuất hiện tại đây."
                )
            } else if (filtered.isEmpty()) {
                HistoryEmptyState(
                    title = "Không tìm thấy kết quả",
                    subtitle = "Thử nhập từ khóa khác để tìm bản quét."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { scan ->
                        HistoryScanCard(
                            scan = scan,
                            onClick = { onScanClick(scan.id) },
                            onDeleteClick = { scanToDelete = scan }
                        )
                    }
                }
            }
        }
    }

    scanToDelete?.let { scan ->
        AlertDialog(
            onDismissRequest = { scanToDelete = null },
            icon = { Icon(Icons.Filled.DeleteOutline, null, tint = Color(0xFFDC5265)) },
            title = { Text("Xóa bản quét?", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc muốn xóa bản quét này khỏi lịch sử LifeLens? Hành động không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteScan(scan)
                    scanToDelete = null
                }) { Text("Xóa", color = Color(0xFFDC5265), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { scanToDelete = null }) { Text("Hủy") }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun HistoryScanCard(
    scan: ScanEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(width = 88.dp, height = 98.dp).clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFFF0F2FF)),
                contentAlignment = Alignment.Center
            ) {
                if (scan.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = Uri.parse(scan.imageUri),
                        contentDescription = "Ảnh bản quét",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Filled.Description, null, tint = Blue, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    scan.text.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.take(70)
                        ?: "Bản quét không có tiêu đề",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Text(scan.text.lineSequence().filter { it.isNotBlank() }.drop(1)
                    .joinToString(" ").ifBlank { scan.text }.take(140),
                    fontSize = 12.sp, color = Muted, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 17.sp
                )
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, null, tint = Violet, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())
                            .format(Date(scan.createdAt)),
                        fontSize = 10.sp, color = Muted
                    )
                }
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(35.dp)) {
                Icon(Icons.Filled.DeleteOutline, "Xóa bản quét", tint = Muted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun HistoryEmptyState(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize().padding(horizontal = 28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(108.dp).clip(CircleShape).background(
                    Brush.radialGradient(listOf(Color(0xFFE2E8FF), Color(0xFFF4EDFF)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = Violet, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text(subtitle, fontSize = 13.sp, color = Muted, lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
