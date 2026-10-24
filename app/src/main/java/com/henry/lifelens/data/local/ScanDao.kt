package com.henry.lifelens.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete

@Dao
interface ScanDao {

    // =========================
    // INSERT
    // =========================

    @Insert
    suspend fun insertScan(
        scan: ScanEntity
    )


    // =========================
    // LẤY TOÀN BỘ LỊCH SỬ
    // =========================

    @Query(
        """
        SELECT *
        FROM scans
        ORDER BY createdAt DESC
        """
    )
    fun getAllScans(): Flow<List<ScanEntity>>


    // =========================
    // LẤY 2 SCAN GẦN NHẤT
    // =========================

    @Query(
        """
        SELECT *
        FROM scans
        ORDER BY createdAt DESC
        LIMIT 2
        """
    )
    fun getRecentScans(): Flow<List<ScanEntity>>


    @Query(
        """
        SELECT *
        FROM scans
        WHERE id = :scanId
        LIMIT 1
        """
    )
    suspend fun getScanById(
        scanId: Long
    ): ScanEntity?

    @Delete
    suspend fun deleteScan(
        scan: ScanEntity
    )
}