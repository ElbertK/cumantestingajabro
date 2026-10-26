package com.deskpal.reminder

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("deskpal_prefs", Context.MODE_PRIVATE)
        val meetingMode = prefs.getBoolean("meeting_mode", false)
        val meetingEnd = prefs.getLong("meeting_end", 0L)

        // Auto-expire Meeting Mode if timer elapsed
        if (meetingMode && meetingEnd > 0L && System.currentTimeMillis() > meetingEnd) {
            prefs.edit().putBoolean("meeting_mode", false).putLong("meeting_end", 0L).apply()
        } else if (meetingMode && intent.action == "com.deskpal.reminder.ACTION_TRIGGER_REMINDER") {
            // Silently skip alert while in Meeting Mode
            return
        }

        val reminderId = intent.getStringExtra("REMINDER_ID") ?: "drink_water"
        val item = defaultReminders().find { it.id == reminderId } ?: defaultReminders().first()

        when (intent.action) {
            "com.deskpal.reminder.ACTION_SNOOZE_15" -> {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(reminderId.hashCode())
                ReminderScheduler.scheduleNextOccurrence(context, item, customDelayMinutes = item.snoozeMinutes)
            }
            "com.deskpal.reminder.ACTION_MARK_DONE" -> {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(reminderId.hashCode())
                ReminderScheduler.scheduleNextOccurrence(context, item)
            }
            "com.deskpal.reminder.ACTION_TOGGLE_MEETING" -> {
                val nextState = !meetingMode
                prefs.edit().putBoolean("meeting_mode", nextState).apply()
            }
            "com.deskpal.reminder.ACTION_TRIGGER_REMINDER" -> {
                dispatchSmartOfficeAlert(context, item, prefs.getBoolean("privacy_labels", true))
            }
        }
    }

    private fun dispatchSmartOfficeAlert(context: Context, item: ReminderItem, usePrivacyLabel: Boolean) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        // Detect whether user is actively using phone vs screen off / locked on desk
        val isScreenInteractive = powerManager.isInteractive && !keyguardManager.isKeyguardLocked

        if (item.gentleVibration) {
            triggerGentleHeartbeatVibration(context)
        }
        if (item.cameraTorchFlash && !isScreenInteractive) {
            triggerSoftTorchBlink(context)
        }

        val displayTitle = if (usePrivacyLabel) item.discreetTitle else item.title
        val channelId = if (item.softSound) "deskpal_chime_channel" else "deskpal_silent_flash_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                if (item.softSound) "DeskPal Soft Chime Alerts" else "DeskPal Silent Visual Alerts",
                NotificationManager.IMPORTANCE_HIGH // Required for iPhone-style Top Heads-Up Banner & Full-Screen Intent
            ).apply {
                if (!item.softSound) {
                    setSound(null, null)
                }
                enableVibration(item.gentleVibration)
            }
            nm.createNotificationChannel(channel)
        }

        // Action Intents: Done & Snooze +15m
        val doneIntent = PendingIntent.getBroadcast(
            context, item.id.hashCode() + 1,
            Intent(context, AlarmReceiver::class.java).apply {
                action = "com.deskpal.reminder.ACTION_MARK_DONE"
                putExtra("REMINDER_ID", item.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = PendingIntent.getBroadcast(
            context, item.id.hashCode() + 2,
            Intent(context, AlarmReceiver::class.java).apply {
                action = "com.deskpal.reminder.ACTION_SNOOZE_15"
                putExtra("REMINDER_ID", item.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full-Screen Activity Intent (for when screen is OFF)
        val fullScreenIntent = Intent(context, FullScreenAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("REMINDER_ID", item.id)
            putExtra("DISPLAY_TITLE", displayTitle)
            putExtra("EMOJI", item.emoji)
            putExtra("SNOOZE_MINS", item.snoozeMinutes)
            putExtra("FLASH_COLOR", item.flashColorHex)
        }
        val fullScreenPi = PendingIntent.getActivity(
            context, item.id.hashCode() + 3,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("${item.emoji} $displayTitle")
            .setContentText("Repeat ${item.completedToday + 1}/${item.maxRepeatsPerDay} today • Silent Office Mode")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Done ✓", doneIntent)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Remind in ${item.snoozeMinutes}m", snoozeIntent)

        if (!item.softSound) {
            builder.setSilent(true)
        }

        // Attach Full-Screen Intent so Android automatically pops up FullScreenAlertActivity when screen is OFF,
        // and shows a top Heads-Up Banner (like iPhone alarm banner) when screen is ON!
        if (item.fullScreenWhenOff) {
            builder.setFullScreenIntent(fullScreenPi, true)
        }

        nm.notify(item.id.hashCode(), builder.build())

        // On some OEMs when screen is off, also explicitly launch activity if overlay permission is granted
        if (!isScreenInteractive && item.fullScreenWhenOff) {
            try {
                context.startActivity(fullScreenIntent)
            } catch (_: Exception) {
                // Handled by setFullScreenIntent fallback
            }
        }
    }

    private fun triggerGentleHeartbeatVibration(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Soft double-pulse heartbeat (low amplitude so it doesn't rattle a wooden office desk)
            val timings = longArrayOf(0, 70, 120, 90)
            val amplitudes = intArrayOf(0, 65, 0, 85)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        }
    }

    private fun triggerSoftTorchBlink(context: Context) {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cm.cameraIdList.firstOrNull() ?: return
        val handler = Handler(Looper.getMainLooper())
        try {
            cm.setTorchMode(cameraId, true)
            handler.postDelayed({ runCatching { cm.setTorchMode(cameraId, false) } }, 180)
            handler.postDelayed({ runCatching { cm.setTorchMode(cameraId, true) } }, 360)
            handler.postDelayed({ runCatching { cm.setTorchMode(cameraId, false) } }, 540)
        } catch (_: Exception) {}
    }
}
