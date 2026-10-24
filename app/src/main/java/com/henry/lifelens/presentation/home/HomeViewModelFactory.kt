package com.henry.lifelens.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henry.lifelens.data.repository.ReminderRepository
import com.henry.lifelens.data.repository.ScanRepository

class HomeViewModelFactory(
    private val scanRepository: ScanRepository,
    private val reminderRepository: ReminderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                HomeViewModel::class.java
            )
        ) {
            return HomeViewModel(
                scanRepository = scanRepository,
                reminderRepository = reminderRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}