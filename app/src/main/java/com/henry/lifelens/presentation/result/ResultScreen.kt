package com.henry.lifelens.presentation.result

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.henry.lifelens.data.storage.ImageStorage
import com.henry.lifelens.util.SmartActionDetector

private val LifeLensBlue = Color(0xFF5865F2)
private val LifeLensViolet = Color(0xFF8B5CF6)
private val LifeLensCyan = Color(0xFF22D3EE)
private val LifeLensBackground = Color(0xFFF7F8FC)
private val LifeLensSoftBlue = Color(0xFFF0F2FF)
private val LifeLensText = Color(0xFF111827)
private val LifeLensSecondaryText = Color(0xFF7C8496)
private val LensGradient = Brush.linearGradient(listOf(LifeLensCyan, LifeLensBlue, LifeLensViolet))

@Composable
fun ResultScreen(
    imageUri: Uri?,
    recognizedText: String,
    viewModel: ResultViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val isSaved by viewModel.isSaved.collectAsState()
    val reminderMessage by viewModel.reminderMessage.collectAsState()
    var editedText by remember(recognizedText) { mutableStateOf(recognizedText) }
    var showReminderSheet by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    val actions = remember(editedText) { SmartActionDetector.detect(editedText) }
    val hasActions = actions.date != null || actions.time != null ||
            actions.phoneNumber != null || actions.url != null || actions.address != null

    LaunchedEffect(reminderMessage) {
        reminderMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearReminderMessage()
        }
    }

    Box(Modifier.fillMaxSize().background(LifeLensBackground)) {
        Box(Modifier.fillMaxWidth().height(350.dp).background(
            Brush.verticalGradient(listOf(Color(0xFFE3EDFF), Color(0xFFF3EEFF), LifeLensBackground))
        ))
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 20.dp, bottom = 32.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color.White, shadowElevation = 3.dp) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", tint = LifeLensText)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Kết quả phân tích", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                        color = LifeLensText)
                    Text("LIFELENS • AI INSIGHT", fontSize = 10.sp, letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold, color = LifeLensBlue)
                }
                Box(Modifier.size(44.dp).clip(CircleShape).background(LensGradient),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.AutoAwesome, "AI", tint = Color.White)
                }
            }
            Spacer(Modifier.height(24.dp))
            Card(shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)) {
                Column {
                    if (imageUri != null) {
                        Box(Modifier.fillMaxWidth().height(205.dp).clickable { showFullImage = true }) {
                            AsyncImage(model = imageUri, contentDescription = "Ảnh đã quét",
                                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            Surface(Modifier.align(Alignment.BottomEnd).padding(12.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = .64f)) {
                                Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ZoomIn, null, tint = Color.White,
                                        modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Xem ảnh", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF16A085))
                        Spacer(Modifier.width(9.dp))
                        Column {
                            Text("Đã nhận diện văn bản", color = LifeLensText,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Bạn có thể chỉnh sửa trước khi lưu", fontSize = 12.sp,
                                color = LifeLensSecondaryText)
                        }
                    }
                }
            }
            Spacer(Modifier.height(23.dp))
            SectionTitle("Văn bản nhận diện", "CHỈNH SỬA NỘI DUNG")
            Spacer(Modifier.height(11.dp))
            Card(shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = editedText, onValueChange = { editedText = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                        placeholder = { Text("Văn bản nhận diện...") },
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LifeLensBlue,
                            unfocusedBorderColor = Color(0xFFE3E6F0))
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = {
                            clipboard.setText(AnnotatedString(editedText))
                            Toast.makeText(context, "Đã sao chép văn bản", Toast.LENGTH_SHORT).show()
                        }, enabled = editedText.isNotBlank(), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp)); Text("Sao chép", fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, editedText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Chia sẻ văn bản"))
                        }, enabled = editedText.isNotBlank(), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Share, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp)); Text("Chia sẻ", fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(25.dp))
            SectionTitle("AI Insight", "HÀNH ĐỘNG THÔNG MINH")
            Spacer(Modifier.height(10.dp))
            Text("LifeLens gợi ý hành động từ nội dung được nhận diện.",
                color = LifeLensSecondaryText, fontSize = 13.sp)
            Spacer(Modifier.height(13.dp))
            if (!hasActions) {
                Card(shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, null, tint = LifeLensViolet)
                        Spacer(Modifier.width(10.dp))
                        Text("Chưa phát hiện ngày giờ, số điện thoại, liên kết hoặc địa chỉ.",
                            fontSize = 13.sp, color = LifeLensSecondaryText)
                    }
                }
            }
            if (actions.date != null || actions.time != null) {
                InsightCard(Icons.Filled.CalendarMonth, "Tạo nhắc nhở",
                    listOfNotNull(actions.date, actions.time).joinToString(" • ")) {
                    showReminderSheet = true
                }
            }
            actions.phoneNumber?.let { phone ->
                InsightCard(Icons.Filled.Phone, "Gọi điện thoại", phone) {
                    try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) }
                    catch (_: Exception) { Toast.makeText(context, "Không thể mở ứng dụng điện thoại", Toast.LENGTH_SHORT).show() }
                }
            }
            actions.url?.let { url ->
                InsightCard(Icons.Filled.Link, "Mở liên kết", url) {
                    try {
                        val target = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                    } catch (_: Exception) {
                        Toast.makeText(context, "Không thể mở liên kết", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            actions.address?.let { address ->
                InsightCard(Icons.Filled.LocationOn, "Mở bản đồ", address) {
                    val encoded = Uri.encode(address)
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")))
                    } catch (_: Exception) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=$encoded")))
                        } catch (_: Exception) {
                            Toast.makeText(context, "Không thể mở bản đồ", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            Spacer(Modifier.height(25.dp))
            Button(
                onClick = {
                    if (imageUri != null) {
                        try {
                            val savedUri = ImageStorage.saveImage(context = context, sourceUri = imageUri)
                            viewModel.saveScan(editedText, savedUri.toString())
                        } catch (_: Exception) {
                            Toast.makeText(context, "Không thể lưu hình ảnh", Toast.LENGTH_SHORT).show()
                        }
                    } else viewModel.saveScan(editedText, "")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isSaved && editedText.isNotBlank(),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LifeLensBlue)
            ) {
                Icon(if (isSaved) Icons.Filled.CheckCircle else Icons.Filled.Save,
                    null, modifier = Modifier.size(21.dp))
                Spacer(Modifier.width(9.dp))
                Text(if (isSaved) "Đã lưu kết quả" else "Lưu kết quả",
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
    if (showFullImage && imageUri != null) {
        AlertDialog(
            onDismissRequest = { showFullImage = false },
            confirmButton = { TextButton(onClick = { showFullImage = false }) { Text("Đóng") } },
            text = { AsyncImage(model = imageUri, contentDescription = "Ảnh gốc",
                modifier = Modifier.fillMaxWidth().heightIn(max = 540.dp),
                contentScale = ContentScale.Fit) }
        )
    }
    if (showReminderSheet) {
        CreateReminderSheet(
            detectedText = editedText,
            detectedDate = actions.date,
            detectedTime = actions.time,
            detectedAddress = actions.address,
            onDismiss = { showReminderSheet = false },
            onCreateReminder = { title, date, time, location ->
                viewModel.createReminder(title, date, time, location)
                showReminderSheet = false
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, eyebrow: String) {
    Column {
        Text(eyebrow, fontSize = 10.sp, letterSpacing = 1.3.sp,
            fontWeight = FontWeight.Bold, color = LifeLensBlue)
        Spacer(Modifier.height(4.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LifeLensText)
    }
}

@Composable
private fun InsightCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp))
                .background(LifeLensSoftBlue), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = LifeLensBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = LifeLensText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = LifeLensSecondaryText, fontSize = 12.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = LifeLensViolet)
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun CreateReminderSheet(
    detectedText: String,
    detectedDate: String?,
    detectedTime: String?,
    detectedAddress: String?,

    onDismiss: () -> Unit,

    onCreateReminder: (
        title: String,
        date: String,
        time: String,
        location: String
    ) -> Unit
) {

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded =
                true
        )
    var title by remember {

        mutableStateOf(

            detectedText
                .lineSequence()
                .firstOrNull {
                    it.isNotBlank()
                }
                ?.take(60)
                ?: ""
        )
    }


    var date by remember {

        mutableStateOf(
            detectedDate ?: ""
        )
    }


    var time by remember {

        mutableStateOf(
            detectedTime ?: ""
        )
    }


    var location by remember(detectedAddress) {
        mutableStateOf(
            detectedAddress ?: ""
        )
    }


    ModalBottomSheet(

        onDismissRequest =
            onDismiss,

        sheetState =
            sheetState,

        containerColor =
            Color.White
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 32.dp
                    )
        ) {

            Text(
                text =
                    "Tạo nhắc nhở",

                fontSize =
                    22.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    LifeLensText
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(
                text =
                    "LifeLens đã điền thông tin được phát hiện từ hình ảnh.",

                fontSize =
                    14.sp,

                color =
                    LifeLensSecondaryText
            )


            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            ReminderFieldTitle(
                text =
                    "Tên sự kiện"
            )


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            OutlinedTextField(
                value =
                    title,

                onValueChange = {
                    title = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine =
                    true,

                placeholder = {

                    Text(
                        text =
                            "Nhập tên sự kiện"
                    )
                }
            )


            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

        }
        ReminderFieldTitle(
            text =
                "Ngày"
        )


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )


        OutlinedTextField(
            value =
                date,

            onValueChange = {
                date = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine =
                true,

            placeholder = {

                Text(
                    text =
                        "dd/MM/yyyy"
                )
            },

            leadingIcon = {

                Icon(
                    imageVector =
                        Icons.Filled
                            .CalendarMonth,

                    contentDescription =
                        null
                )
            }
        )


        Spacer(
            modifier =
                Modifier.height(18.dp)
        )


        ReminderFieldTitle(
            text =
                "Giờ"
        )


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )


        OutlinedTextField(
            value =
                time,

            onValueChange = {
                time = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine =
                true,

            placeholder = {

                Text(
                    text =
                        "HH:mm"
                )
            }
        )

        ReminderFieldTitle(
            text =
                "Địa điểm"
        )


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )


        OutlinedTextField(
            value =
                location,

            onValueChange = {
                location = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine =
                true,

            placeholder = {

                Text(
                    text =
                        "Thêm địa điểm"
                )
            }
        )


        Spacer(
            modifier =
                Modifier.height(26.dp)
        )


        Button(

            onClick = {

                onCreateReminder(
                    title,
                    date,
                    time,
                    location
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(54.dp),

            enabled =
                title.isNotBlank() &&
                        date.isNotBlank() &&
                        time.isNotBlank(),

            shape =
                RoundedCornerShape(
                    14.dp
                )
        ) {

            Text(
                text =
                    "Tạo nhắc nhở",

                fontWeight =
                    FontWeight.SemiBold
            )
        }


        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        TextButton(
            onClick =
                onDismiss,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    "Hủy",

                color =
                    LifeLensSecondaryText
            )
        }
    }
}



@Composable
private fun ReminderFieldTitle(
    text: String
) {

    Text(
        text =
            text,

        fontSize =
            13.sp,

        fontWeight =
            FontWeight.SemiBold,

        color =
            LifeLensText
    )
}
