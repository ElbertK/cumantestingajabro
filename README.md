# DeskPal — Cute Office Wellness & Silent Alarm Reminder (Android 10 to Android 16 / Samsung S25)

DeskPal is a native Android application built with **Kotlin**, **Jetpack Compose**, and **Glance AppWidget** designed specifically for office workers who keep their phone muted on their desk.

## Compatibility (2021–2026 Phones)
- **minSdk = 26 (Android 8.0)** / Tested & optimized for **API 29 (Android 10, 2020/2021)** through **API 35+ (Android 15/16, Samsung Galaxy S25 One UI 7+)**.
- **Dual-Mode Alert Routing**:
  - **Screen OFF / Locked**: Automatically launches `FullScreenAlertActivity` over the lock screen (`setShowWhenLocked(true)`, `setTurnScreenOn(true)`), flashing a soft pastel edge/screen glow and pulsing cute animations so you notice it out of the corner of your eye without a single sound.
  - **Screen ON / Active Use**: Detects interactive state via `PowerManager.isInteractive` and `KeyguardManager.isKeyguardLocked` and displays a high-priority **iPhone-style Top Heads-Up Banner Notification** with inline **Done** and **+15m Snooze** buttons so it never interrupts your active typing or browsing.

## Key Office-Friendly Features
1. **Per-Reminder Sensory Matrix**: Independently toggle **Full-Screen Wake**, **Screen Edge Flash**, **Camera LED Torch Blink**, **Gentle Heartbeat Vibration**, and **Soft Chime Sound** (defaults to 100% silent visual flash).
2. **Interval & Daily Repeat Scheduler**: Set active work hours (e.g., 09:00–17:30), interval minutes (e.g., every 45 mins), and max daily repeats (e.g., 8 times/day).
3. **Meeting Mode (1-Tap Pause + Auto-Resume)**: Pause all reminders indefinitely or for 30m / 1h / 2h so your phone never flashes when a colleague or boss is looking at your desk.
4. **Discreet Privacy / Ghost Labels**: Replace "Toilet Break" or personal reminders with cute code words on screen ("Desk Stretch ✨" or "Quick Step Away 🌱").
5. **Pocket / Face-Down Guard**: Uses the proximity sensor to suppress bright flashes if the phone is inside a bag or lying face-down.
6. **Home Screen Widget**: Includes a native Glance / RemoteViews widget for 1-tap Meeting Mode toggle, Quick +1 Water log, and next reminder countdown.

## How to Build & Install on Your Samsung S25
1. Open **Android Studio Ladybug (2024.2.1)** or newer.
2. Select **Open** and choose this `deskpal_android_project` folder.
3. Let Gradle sync, connect your Samsung Galaxy S25 (with USB Debugging enabled), and click **Run ('app')**.
4. **Important Samsung S25 / Android 14+ Permissions**:
   - On first launch, tap the **Permissions Checklist banner** inside DeskPal to grant:
     - **Post Notifications** (Android 13+)
     - **Full-Screen Intent** (`Settings > Apps > Special access > Full screen alerts` on Android 14/15+)
     - **Exact Alarms** (`Schedule exact alarms`)
   - Optional for Samsung One UI: Set Battery Usage to **Unrestricted** under `App Info > Battery` so One UI Deep Sleep never delays your hydration breaks.
