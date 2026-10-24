package com.henry.lifelens.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henry.lifelens.data.repository.ReminderRepository

class ReminderViewModelFactory(
    private val reminderRepository: ReminderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                ReminderViewModel::class.java
            )
        ) {
            return ReminderViewModel(
                reminderRepository = reminderRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}