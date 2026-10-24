package com.henry.lifelens.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "scans"
)
data class ScanEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val text: String,

    val imageUri: String,

    val createdAt: Long
)