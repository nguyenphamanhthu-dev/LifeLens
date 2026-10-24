package com.henry.lifelens.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henry.lifelens.data.repository.ScanRepository

class HistoryViewModelFactory(
    private val repository: ScanRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                HistoryViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}