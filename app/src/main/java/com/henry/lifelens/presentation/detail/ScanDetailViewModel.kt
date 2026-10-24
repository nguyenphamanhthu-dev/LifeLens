package com.henry.lifelens.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.henry.lifelens.data.local.ScanEntity
import com.henry.lifelens.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanDetailViewModel(
    private val repository: ScanRepository
) : ViewModel() {

    private val _scan =
        MutableStateFlow<ScanEntity?>(null)

    val scan: StateFlow<ScanEntity?> =
        _scan.asStateFlow()


    fun loadScan(
        scanId: Long
    ) {

        viewModelScope.launch {

            _scan.value =
                repository.getScanById(
                    scanId
                )
        }
    }
}