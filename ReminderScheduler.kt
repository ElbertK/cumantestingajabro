package com.deskpal.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    fun scheduleNextOccurrence(context: Context, item: ReminderItem, customDelayMinutes: Int? = null) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.deskpal.reminder.ACTION_TRIGGER_REMINDER"
            putExtra("REMINDER_ID", item.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!item.isEnabled || item.completedToday >= item.maxRepeatsPerDay) {
            alarmManager.cancel(pendingIntent)
            return
        }

        val delayMins = customDelayMinutes ?: item.intervalMinutes
        val triggerAtMillis = System.currentTimeMillis() + (delayMins * 60 * 1000L)

        // Check if within active work hours
        val cal = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        val minsOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val startMins = item.startHour * 60 + item.startMinute
        val endMins = item.endHour * 60 + item.endMinute

        if (minsOfDay !in startMins..endMins) {
            // Schedule for tomorrow's start time
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, item.startHour)
                set(Calendar.MINUTE, item.startMinute)
                set(Calendar.SECOND, 0)
            }
            setExactCompatibleAlarm(alarmManager, tomorrow.timeInMillis, pendingIntent)
        } else {
            setExactCompatibleAlarm(alarmManager, triggerAtMillis, pendingIntent)
        }
    }

    private fun setExactCompatibleAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        operation: PendingIntent
    ) {
        // Supports API 26 through API 35+ (Samsung S25 Android 15+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, operation)
                alarmManager.setAlarmClock(alarmClockInfo, operation)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            }
        } else {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, operation)
            alarmManager.setAlarmClock(alarmClockInfo, operation)
        }
    }

    fun cancelAll(context: Context, items: List<ReminderItem>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        items.forEach { item ->
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.deskpal.reminder.ACTION_TRIGGER_REMINDER"
            }
            val pi = PendingIntent.getBroadcast(
                context,
                item.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pi)
        }
    }
}
