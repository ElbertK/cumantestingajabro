package com.deskpal.reminder

data class ReminderItem(
    val id: String,
    val title: String,
    val discreetTitle: String, // Shown when Office Privacy / Ghost Label mode is ON
    val emoji: String,
    val category: String, // "HYDRATION", "RESTROOM", "POSTURE", "EYES"
    val intervalMinutes: Int = 45,
    val maxRepeatsPerDay: Int = 8,
    val completedToday: Int = 0,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 30,
    val snoozeMinutes: Int = 15,
    val isEnabled: Boolean = true,
    // Sensory Matrix (Office-Friendly Defaults: Visual Flash ON, Sound OFF)
    val fullScreenWhenOff: Boolean = true,
    val screenEdgeFlash: Boolean = true,
    val cameraTorchFlash: Boolean = false,
    val gentleVibration: Boolean = true,
    val softSound: Boolean = false,
    val flashColorHex: String = "#7DD3FC"
)

data class AppState(
    val meetingModeActive: Boolean = false,
    val meetingModeEndTimeMillis: Long = 0L, // 0L = until manually turned off
    val privacyLabelsEnabled: Boolean = true,
    val pocketGuardEnabled: Boolean = true,
    val reminders: List<ReminderItem> = defaultReminders()
)

fun defaultReminders(): List<ReminderItem> = listOf(
    ReminderItem(
        id = "drink_water",
        title = "Sip Water & Hydrate",
        discreetTitle = "Desk Refresh ✨",
        emoji = "💧",
        category = "HYDRATION",
        intervalMinutes = 45,
        maxRepeatsPerDay = 8,
        completedToday = 3,
        snoozeMinutes = 15,
        fullScreenWhenOff = true,
        screenEdgeFlash = true,
        cameraTorchFlash = false,
        gentleVibration = true,
        softSound = false,
        flashColorHex = "#38BDF8"
    ),
    ReminderItem(
        id = "toilet_break",
        title = "Toilet & Stretch Break",
        discreetTitle = "Step Away & Breathe 🌱",
        emoji = "🚻",
        category = "RESTROOM",
        intervalMinutes = 120,
        maxRepeatsPerDay = 4,
        completedToday = 1,
        snoozeMinutes = 15,
        fullScreenWhenOff = true,
        screenEdgeFlash = true,
        cameraTorchFlash = true,
        gentleVibration = true,
        softSound = false,
        flashColorHex = "#F472B6"
    ),
    ReminderItem(
        id = "eye_posture",
        title = "20-20-20 Eye & Posture Reset",
        discreetTitle = "Look Far & Relax Shoulders 🌸",
        emoji = "👀",
        category = "EYES",
        intervalMinutes = 60,
        maxRepeatsPerDay = 6,
        completedToday = 2,
        snoozeMinutes = 10,
        fullScreenWhenOff = true,
        screenEdgeFlash = true,
        cameraTorchFlash = false,
        gentleVibration = false,
        softSound = false,
        flashColorHex = "#A78BFA"
    )
)
