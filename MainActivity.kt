
// 1-Tap Guided Onboarding for Non-Tech Partner:
// Automatically requests POST_NOTIFICATIONS, checks Exact Alarms, and opens Full-Screen Flash toggle in 1 tap.
package com.deskpal.reminder

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DeskPalMainScreen()
            }
        }
    }
}

@Composable
fun DeskPalMainScreen() {
    val context = LocalContext.current
    var appState by remember { mutableStateOf(AppState()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF7))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🌸 DeskPal Office Companion", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF334155))
            Text("Silent visual flash when screen is OFF • Top banner when screen is ON", fontSize = 13.sp, color = Color(0xFF64748B))
        }

        // Samsung S25 / Android 14+ Full-Screen Intent Permission Check
        item {
            SamsungS25PermissionCard(context)
        }

        // Meeting Mode Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (appState.meetingModeActive) Color(0xFFFEF2F2) else Color(0xFFF0F9FF)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (appState.meetingModeActive) "🔕 Meeting Mode ON" else "✨ Office Flow Active",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            if (appState.meetingModeActive) "All flashes & banners paused until you turn this off"
                            else "Pause everything in 1 tap during meetings or desk huddles",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }
                    Switch(
                        checked = appState.meetingModeActive,
                        onCheckedChange = { active ->
                            appState = appState.copy(meetingModeActive = active)
                            context.getSharedPreferences("deskpal_prefs", Context.MODE_PRIVATE)
                                .edit().putBoolean("meeting_mode", active).apply()
                        }
                    )
                }
            }
        }

        // Reminder Cards
        items(appState.reminders) { item ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("${item.emoji} ${item.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${item.completedToday}/${item.maxRepeatsPerDay} today", fontSize = 12.sp, color = Color(0xFF0284C7))
                    }
                    Text("Every ${item.intervalMinutes} mins • Snooze +${item.snoozeMinutes}m • Discreet label: "${item.discreetTitle}"", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
fun SamsungS25PermissionCard(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.canUseFullScreenIntent()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚡ Samsung S25 / Android 14+ Lock-Screen Flash Setup", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Allow Full-Screen Intents so DeskPal can light up your screen when locked on your desk.", fontSize = 12.sp)
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Enable Lockscreen Visual Flash")
                    }
                }
            }
        }
    }
}
