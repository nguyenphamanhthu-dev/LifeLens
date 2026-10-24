package com.henry.lifelens.presentation.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henry.lifelens.data.repository.ReminderRepository
import com.henry.lifelens.data.repository.ScanRepository
import com.henry.lifelens.notification.ReminderScheduler


class ResultViewModelFactory(
    private val scanRepository: ScanRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                ResultViewModel::class.java
            )
        ) {

            return ResultViewModel(
                scanRepository = scanRepository,
                reminderRepository = reminderRepository,
                reminderScheduler = reminderScheduler
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}