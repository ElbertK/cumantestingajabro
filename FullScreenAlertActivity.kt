package com.deskpal.reminder

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class FullScreenAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableLockScreenWakeUp()

        val reminderId = intent.getStringExtra("REMINDER_ID") ?: "drink_water"
        val title = intent.getStringExtra("DISPLAY_TITLE") ?: "Desk Refresh ✨"
        val emoji = intent.getStringExtra("EMOJI") ?: "💧"
        val snoozeMins = intent.getIntExtra("SNOOZE_MINS", 15)

        setContent {
            FullScreenFlashOverlay(
                emoji = emoji,
                title = title,
                snoozeMins = snoozeMins,
                onDone = {
                    sendBroadcast(Intent(this, AlarmReceiver::class.java).apply {
                        action = "com.deskpal.reminder.ACTION_MARK_DONE"
                        putExtra("REMINDER_ID", reminderId)
                    })
                    finish()
                },
                onSnooze = { mins ->
                    sendBroadcast(Intent(this, AlarmReceiver::class.java).apply {
                        action = "com.deskpal.reminder.ACTION_SNOOZE_15"
                        putExtra("REMINDER_ID", reminderId)
                        putExtra("CUSTOM_SNOOZE", mins)
                    })
                    finish()
                }
            )
        }
    }

    private fun enableLockScreenWakeUp() {
        // Compatible across Android 8.0 (2021 phones) through Android 15/16 (Samsung S25)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun FullScreenFlashOverlay(
    emoji: String,
    title: String,
    snoozeMins: Int,
    onDone: () -> Unit,
    onSnooze: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "edgeFlash")
    val glowColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF1E293B),
        targetValue = Color(0xFF38BDF8),
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .border(width = 14.dp, color = glowColor, shape = RoundedCornerShape(32.dp))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                color = glowColor.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 56.sp)
                }
            }

            Text(
                text = "SILENT DESK FLASH",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Zero sound • Only visual pulse so you don't disturb your office",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(58.dp)
            ) {
                Text("Done! Back to Flow ✓", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onSnooze(5) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("+5m Snooze", color = Color.White)
                }
                OutlinedButton(
                    onClick = { onSnooze(snoozeMins) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("+$snoozeMins mins", color = Color.White)
                }
            }
        }
    }
}
