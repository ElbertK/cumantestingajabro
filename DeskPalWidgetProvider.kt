package com.deskpal.reminder

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class DeskPalWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("deskpal_prefs", Context.MODE_PRIVATE)
        val meetingMode = prefs.getBoolean("meeting_mode", false)

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, android.R.layout.simple_list_item_2)
            views.setTextViewText(
                android.R.id.text1,
                if (meetingMode) "🔕 Meeting Mode ON (Reminders Paused)" else "💧 DeskPal Active • Next break in 18m"
            )
            views.setTextViewText(
                android.R.id.text2,
                "Tap to toggle Meeting Mode or log water break"
            )

            val toggleIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.deskpal.reminder.ACTION_TOGGLE_MEETING"
            }
            val pi = PendingIntent.getBroadcast(
                context, 999, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(android.R.id.text1, pi)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
