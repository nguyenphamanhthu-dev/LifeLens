package com.henry.lifelens.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henry.lifelens.data.local.ScanEntity
import com.henry.lifelens.data.repository.ScanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: ScanRepository
) : ViewModel() {

    val scans: StateFlow<List<ScanEntity>> =
        repository
            .getAllScans()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    fun deleteScan(
        scan: ScanEntity
    ) {

        viewModelScope.launch {

            repository.deleteScan(
                scan
            )
        }
    }
}