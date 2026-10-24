package com.henry.lifelens.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henry.lifelens.data.local.ReminderEntity
import com.henry.lifelens.data.repository.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReminderViewModel(
    reminderRepository: ReminderRepository
) : ViewModel() {

    val reminders: StateFlow<List<ReminderEntity>> =
        reminderRepository
            .getAllReminders()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}