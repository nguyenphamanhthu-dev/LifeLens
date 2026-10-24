package com.henry.lifelens.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henry.lifelens.data.local.ReminderEntity
import com.henry.lifelens.data.local.ScanEntity
import com.henry.lifelens.data.repository.ReminderRepository
import com.henry.lifelens.data.repository.ScanRepository
import com.henry.lifelens.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel(
    private val scanRepository: ScanRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _isSaved =
        MutableStateFlow(false)

    val isSaved: StateFlow<Boolean> =
        _isSaved.asStateFlow()


    private val _reminderMessage =
        MutableStateFlow<String?>(null)

    val reminderMessage: StateFlow<String?> =
        _reminderMessage.asStateFlow()


    fun saveScan(
        text: String,
        imageUri: String
    ) {

        if (text.isBlank()) {
            return
        }

        viewModelScope.launch {

            try {

                val scan =
                    ScanEntity(
                        text = text,
                        imageUri = imageUri,
                        createdAt =
                            System.currentTimeMillis()
                    )

                scanRepository.insertScan(
                    scan
                )

                _isSaved.value = true

            } catch (exception: Exception) {

                exception.printStackTrace()
            }
        }
    }


    fun createReminder(
        title: String,
        date: String,
        time: String,
        location: String
    ) {
        android.util.Log.e(
            "LifeLensDebug",
            "1 - createReminder ĐÃ ĐƯỢC GỌI: date=$date time=$time"
        )

        _reminderMessage.value = null


        if (
            title.isBlank() ||
            date.isBlank() ||
            time.isBlank()
        ) {

            _reminderMessage.value =
                "Vui lòng nhập đầy đủ tên, ngày và giờ"

            return
        }


        viewModelScope.launch {

            try {

                val triggerAtMillis =
                    reminderScheduler.parseDateTime(
                        date = date.trim(),
                        time = time.trim()
                    )

                if (triggerAtMillis == null) {

                    _reminderMessage.value =
                        "Ngày hoặc giờ không đúng định dạng"

                    return@launch
                }


                if (
                    triggerAtMillis <=
                    System.currentTimeMillis()
                ) {

                    _reminderMessage.value =
                        "Thời gian nhắc nhở phải ở tương lai"

                    return@launch
                }

                val reminder =
                    ReminderEntity(
                        title = title.trim(),
                        date = date.trim(),
                        time = time.trim(),
                        location = location.trim(),
                        triggerAtMillis =
                            triggerAtMillis,
                        createdAt =
                            System.currentTimeMillis()
                    )


                val reminderId =
                    reminderRepository
                        .insertReminder(
                            reminder
                        )


                android.util.Log.e(
                    "LifeLensDebug",
                    "2 - CHUẨN BỊ GỌI SCHEDULER: trigger=$triggerAtMillis"
                )

                val scheduled =
                    reminderScheduler.scheduleReminder(
                        reminderId = reminderId,
                        title = reminder.title,
                        time = reminder.time,
                        location = reminder.location,
                        triggerAtMillis =
                            reminder.triggerAtMillis
                    )


                if (scheduled) {

                    _reminderMessage.value =
                        "Đã tạo nhắc nhở"

                } else {

                    if (
                        !reminderScheduler
                            .canScheduleExactAlarms()
                    ) {

                        _reminderMessage.value =
                            "Hãy cho phép LifeLens đặt báo thức và lời nhắc"

                        reminderScheduler
                            .openExactAlarmSettings()

                    } else {

                        _reminderMessage.value =
                            "Không thể đặt lịch nhắc nhở"
                    }
                }

            } catch (exception: Exception) {

                exception.printStackTrace()

                _reminderMessage.value =
                    "Có lỗi khi tạo nhắc nhở"
            }
        }
    }

    fun clearReminderMessage() {

        _reminderMessage.value = null
    }
}