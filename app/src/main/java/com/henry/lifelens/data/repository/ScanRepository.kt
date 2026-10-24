package com.henry.lifelens.data.repository

import com.henry.lifelens.data.local.ScanDao
import com.henry.lifelens.data.local.ScanEntity
import com.henry.lifelens.data.storage.ImageStorage
import kotlinx.coroutines.flow.Flow

class ScanRepository(
    private val scanDao: ScanDao
) {

    suspend fun insertScan(
        scan: ScanEntity
    ) {
        scanDao.insertScan(scan)
    }

    fun getAllScans(): Flow<List<ScanEntity>> {
        return scanDao.getAllScans()
    }


    fun getRecentScans(): Flow<List<ScanEntity>> {
        return scanDao.getRecentScans()
    }


    suspend fun getScanById(
        scanId: Long
    ): ScanEntity? {

        return scanDao.getScanById(
            scanId
        )
    }

    suspend fun deleteScan(
        scan: ScanEntity
    ) {

        ImageStorage.deleteImage(
            scan.imageUri
        )

        scanDao.deleteScan(
            scan
        )
    }
}