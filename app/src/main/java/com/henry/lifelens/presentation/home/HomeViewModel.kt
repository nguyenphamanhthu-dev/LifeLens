package com.henry.lifelens.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henry.lifelens.data.local.ReminderEntity
import com.henry.lifelens.data.local.ScanEntity
import com.henry.lifelens.data.repository.ReminderRepository
import com.henry.lifelens.data.repository.ScanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class HomeViewModel(
    scanRepository: ScanRepository,
    reminderRepository: ReminderRepository
) : ViewModel() {

    val recentScans: StateFlow<List<ScanEntity>> =
        scanRepository
            .getRecentScans()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    val upcomingReminder: StateFlow<ReminderEntity?> =
        reminderRepository
            .getUpcomingReminder(
                currentTime = System.currentTimeMillis()
            )
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
}