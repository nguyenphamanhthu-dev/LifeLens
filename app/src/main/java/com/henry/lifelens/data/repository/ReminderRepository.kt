package com.henry.lifelens.data.repository

import com.henry.lifelens.data.local.ReminderDao
import com.henry.lifelens.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao
) {

    suspend fun insertReminder(
        reminder: ReminderEntity
    ): Long =
        reminderDao.insertReminder(reminder)


    suspend fun updateReminder(
        reminder: ReminderEntity
    ) =
        reminderDao.updateReminder(reminder)


    suspend fun deleteReminder(
        reminder: ReminderEntity
    ) =
        reminderDao.deleteReminder(reminder)


    fun getAllReminders():
            Flow<List<ReminderEntity>> =
        reminderDao.getAllReminders()


    fun getUpcomingReminder(
        currentTime: Long
    ): Flow<ReminderEntity?> =
        reminderDao.getUpcomingReminder(
            currentTime
        )


    suspend fun getReminderById(
        reminderId: Long
    ): ReminderEntity? =
        reminderDao.getReminderById(
            reminderId
        )
}