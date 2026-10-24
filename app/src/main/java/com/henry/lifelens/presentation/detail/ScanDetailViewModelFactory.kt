package com.henry.lifelens.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.henry.lifelens.data.repository.ScanRepository

class ScanDetailViewModelFactory(
    private val repository: ScanRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                ScanDetailViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return ScanDetailViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}