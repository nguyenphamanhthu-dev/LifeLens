package com.henry.lifelens.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert
    suspend fun insertReminder(
        reminder: ReminderEntity
    ): Long


    @Update
    suspend fun updateReminder(
        reminder: ReminderEntity
    )


    @Delete
    suspend fun deleteReminder(
        reminder: ReminderEntity
    )


    // Tất cả reminder
    @Query(
        """
        SELECT * FROM reminders
        ORDER BY triggerAtMillis ASC
        """
    )
    fun getAllReminders(): Flow<List<ReminderEntity>>


    // Reminder tương lai gần nhất
    @Query(
        """
        SELECT * FROM reminders
        WHERE triggerAtMillis > :currentTime
        ORDER BY triggerAtMillis ASC
        LIMIT 1
        """
    )
    fun getUpcomingReminder(
        currentTime: Long
    ): Flow<ReminderEntity?>


    @Query(
        """
        SELECT * FROM reminders
        WHERE id = :reminderId
        LIMIT 1
        """
    )
    suspend fun getReminderById(
        reminderId: Long
    ): ReminderEntity?
}