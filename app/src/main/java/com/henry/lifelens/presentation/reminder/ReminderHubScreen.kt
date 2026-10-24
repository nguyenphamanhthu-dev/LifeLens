package com.henry.lifelens.presentation.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.henry.lifelens.data.local.ReminderEntity
import com.henry.lifelens.data.repository.ReminderRepository
import com.henry.lifelens.notification.ReminderScheduler
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val accent = Color(0xFF5865F2)

@Composable
fun ReminderHubScreen(repository: ReminderRepository, scheduler: ReminderScheduler, onBackClick: () -> Unit) {
    val reminders by repository.getAllReminders().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<ReminderEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<ReminderEntity?>(null) }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, floatingActionButton = {
        FloatingActionButton(onClick = { editing = null; showEditor = true }, containerColor = accent, contentColor = Color.White) {
            Icon(Icons.Filled.Add, "Thêm nhắc nhở")
        }
    }) { inset ->
        Column(Modifier.fillMaxSize().background(Color(0xFFF7F8FC)).padding(inset)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
                Text("Nhắc nhở của tôi", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text("Tạo và quản lý các lời nhắc của LifeLens", Modifier.padding(horizontal = 20.dp), color = Color.Gray)
            Spacer(Modifier.height(18.dp))
            if (reminders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có nhắc nhở. Nhấn + để tạo mới.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(reminders.sortedBy { it.triggerAtMillis }, key = { it.id }) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) {
                            Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsActive, null, tint = accent)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold)
                                    Text("${item.date} • ${item.time}", color = Color.Gray, fontSize = 12.sp)
                                    if (item.location.isNotBlank()) Text(item.location, color = Color.Gray, fontSize = 12.sp)
                                    if (item.triggerAtMillis <= System.currentTimeMillis()) Text("Đã qua", color = Color.Gray, fontSize = 11.sp)
                                }
                                IconButton(onClick = { editing = item; showEditor = true }) { Icon(Icons.Filled.Edit, "Sửa", tint = accent) }
                                IconButton(onClick = { deleting = item }) { Icon(Icons.Filled.Delete, "Xóa", tint = Color(0xFFD94848)) }
                            }
                        }
                    }
                }
            }
        }
    }
    deleting?.let { item ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Xóa nhắc nhở?") },
            text = { Text("Báo thức tương ứng cũng sẽ được hủy.") },
            confirmButton = { TextButton(onClick = {
                deleting = null
                scope.launch {
                    try { repository.deleteReminder(item); scheduler.cancelReminder(item.id); snackbar.showSnackbar("Đã xóa nhắc nhở") }
                    catch (_: Exception) { snackbar.showSnackbar("Không thể xóa nhắc nhở") }
                }
            }) { Text("Xóa") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Hủy") } })
    }
    if (showEditor) {
        ReminderEditorDialog(editing, onDismiss = { showEditor = false }, onSave = { title, date, time, location ->
            val millis = scheduler.parseDateTime(date, time)
            when {
                title.isBlank() || date.isBlank() || time.isBlank() -> scope.launch { snackbar.showSnackbar("Nhập tên, ngày và giờ") }
                millis == null -> scope.launch { snackbar.showSnackbar("Ngày hoặc giờ không hợp lệ (dd/MM/yyyy, HH:mm)") }
                millis <= System.currentTimeMillis() -> scope.launch { snackbar.showSnackbar("Hãy chọn thời gian trong tương lai") }
                !scheduler.canScheduleExactAlarms() -> { scheduler.openExactAlarmSettings(); scope.launch { snackbar.showSnackbar("Cấp quyền báo thức chính xác rồi thử lại") } }
                else -> {
                    val original = editing
                    showEditor = false
                    scope.launch {
                        try {
                            val item = if (original == null) ReminderEntity(title = title.trim(), date = date.trim(), time = time.trim(), location = location.trim(), triggerAtMillis = millis, createdAt = System.currentTimeMillis())
                            else original.copy(title = title.trim(), date = date.trim(), time = time.trim(), location = location.trim(), triggerAtMillis = millis)
                            if (original == null) {
                                val id = repository.insertReminder(item)
                                val ok = scheduler.scheduleReminder(id, item.title, item.time, item.location, millis)
                                if (!ok) { repository.getReminderById(id)?.let { repository.deleteReminder(it) }; snackbar.showSnackbar("Không thể đặt báo thức") }
                                else snackbar.showSnackbar("Đã tạo nhắc nhở")
                            } else {
                                val ok = scheduler.scheduleReminder(original.id, item.title, item.time, item.location, millis)
                                if (ok) { repository.updateReminder(item); snackbar.showSnackbar("Đã cập nhật nhắc nhở") }
                                else snackbar.showSnackbar("Không thể cập nhật báo thức")
                            }
                        } catch (_: Exception) { snackbar.showSnackbar("Có lỗi khi lưu nhắc nhở") }
                    }
                }
            }
        })
    }
}
@Composable
private fun ReminderEditorDialog(item: ReminderEntity?, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var title by remember(item) { mutableStateOf(item?.title ?: "") }
    var date by remember(item) { mutableStateOf(item?.date ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var time by remember(item) { mutableStateOf(item?.time ?: "") }
    var location by remember(item) { mutableStateOf(item?.location ?: "") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (item == null) "Thêm nhắc nhở" else "Sửa nhắc nhở") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Tên sự kiện") }, singleLine = true)
                OutlinedTextField(date, { date = it }, label = { Text("Ngày dd/MM/yyyy") }, singleLine = true)
                OutlinedTextField(time, { time = it }, label = { Text("Giờ HH:mm") }, singleLine = true)
                OutlinedTextField(location, { location = it }, label = { Text("Địa điểm (không bắt buộc)") }, singleLine = true)
            }
        }, confirmButton = { TextButton(onClick = { onSave(title, date, time, location) }) { Text("Lưu") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } })
}