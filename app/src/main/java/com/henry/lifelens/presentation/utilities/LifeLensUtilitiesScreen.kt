package com.henry.lifelens.presentation.utilities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val blue = Color(0xFF5865F2)
private val violet = Color(0xFF8B5CF6)

/** mode: map | link | phone */
@Composable
fun LifeLensUtilitiesScreen(mode: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var input by remember(mode) { mutableStateOf("") }
    val title = when (mode) { "map" -> "Bản đồ"; "link" -> "Liên kết"; else -> "Điện thoại" }
    val hint = when (mode) { "map" -> "Nhập địa chỉ cần tìm"; "link" -> "https://example.com"; else -> "Nhập số điện thoại" }
    val icon = when (mode) { "map" -> Icons.Filled.LocationOn; "link" -> Icons.Filled.Link; else -> Icons.Filled.Phone }
    val action = when (mode) { "map" -> "Tìm trên bản đồ"; "link" -> "Mở website"; else -> "Mở ứng dụng gọi điện" }
    fun notify(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    fun openIntent(intent: Intent) {
        try { context.startActivity(intent) }
        catch (_: ActivityNotFoundException) { notify("Không tìm thấy ứng dụng phù hợp") }
        catch (_: Exception) { notify("Không thể mở ứng dụng") }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFF7F8FC))) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Box(Modifier.fillMaxWidth().height(150.dp).background(
                Brush.linearGradient(listOf(Color(0xFF22D3EE), blue, violet)), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, Modifier.size(66.dp), tint = Color.White)
            }
            Spacer(Modifier.height(24.dp))
            Text("$title thông minh", fontWeight = FontWeight.Bold, fontSize = 21.sp)
            Spacer(Modifier.height(8.dp))
            Text("Nhập thông tin để LifeLens mở ứng dụng phù hợp.", color = Color.Gray)
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text(hint) }, singleLine = true, shape = RoundedCornerShape(16.dp))
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                val value = input.trim()
                if (value.isBlank()) { notify("Vui lòng nhập thông tin"); return@Button }
                when (mode) {
                    "map" -> {
                        val query = Uri.encode(value)
                        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query"))
                        if (geo.resolveActivity(context.packageManager) != null) openIntent(geo)
                        else openIntent(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")))
                    }
                    "link" -> {
                        val url = if (value.startsWith("https://", true) || value.startsWith("http://", true)) value else "https://$value"
                        val parsed = Uri.parse(url)
                        if (parsed.host.isNullOrBlank() || !parsed.host!!.contains('.')) notify("Địa chỉ website không hợp lệ")
                        else openIntent(Intent(Intent.ACTION_VIEW, parsed))
                    }
                    else -> {
                        val number = value.filter { it.isDigit() || it == '+' }
                        if (number.count { it.isDigit() } !in 9..15 || number.count { it == '+' } > 1 || (number.contains('+') && !number.startsWith('+'))) notify("Số điện thoại không hợp lệ")
                        else openIntent(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
                    }
                }
            }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = blue)) { Text(action) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(input)); notify("Đã sao chép") },
                enabled = input.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Sao chép")
            }
            if (mode == "link") {
                TextButton(onClick = {
                    val url = input.trim().let { if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it" }
                    if (input.isNotBlank()) openIntent(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(Intent.EXTRA_TEXT, url)
                    }, "Chia sẻ liên kết"))
                }, enabled = input.isNotBlank()) { Text("Chia sẻ liên kết") }
            }
        }
    }
}
