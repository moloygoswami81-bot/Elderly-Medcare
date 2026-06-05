package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.MedicationEntity
import java.util.Calendar

object AlarmSchedulerHelper {
    private const val TAG = "AlarmSchedulerHelper"

    fun scheduleMedicationAlarm(context: Context, medication: MedicationEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.MED_ALARM_ACTION"
            putExtra("MED_ID", medication.id)
            putExtra("MED_NAME", medication.name)
            putExtra("MED_DOSAGE", medication.dosage)
            putExtra("MED_VOICE_PHRASE", medication.customVoicePhrase)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medication.id, // Use medication ID as request code to allow individual cancellation
            intent,
            flags
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, medication.hour)
            set(Calendar.MINUTE, medication.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time is before now, schedule it for the next day
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check if we can schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for med: ${medication.name} (ID: ${medication.id}) at ${medication.hour}:${medication.minute}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)
        }
    }

    fun cancelMedicationAlarm(context: Context, medicationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.MED_ALARM_ACTION"
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId,
            intent,
            flags
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm for medication ID: $medicationId")
    }
}
