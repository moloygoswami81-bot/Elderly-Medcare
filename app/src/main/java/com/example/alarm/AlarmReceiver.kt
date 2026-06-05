package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.AdherenceLogEntity
import com.example.data.AppDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Alarm triggered with action: $action")
        
        if (action == "com.example.MED_ALARM_ACTION") {
            val medId = intent.getIntExtra("MED_ID", -1)
            val medName = intent.getStringExtra("MED_NAME") ?: "Medicine"
            val medDosage = intent.getStringExtra("MED_DOSAGE") ?: ""

            Log.d(TAG, "Triggering alarm for med ID: $medId ($medName)")

            // 1. Start the foreground alarm service
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("MED_ID", medId)
                putExtra("MED_NAME", medName)
                putExtra("MED_DOSAGE", medDosage)
            }
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AlarmService: ${e.message}", e)
            }

            // 2. Perform DB operations asynchronously: record log & schedule tomorrow's alarm
            val pendingResult = goAsync()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val med = db.medicationDao.getMedicationById(medId)
                    
                    if (med != null && med.isActive) {
                        // Create tomorrow's repetition
                        AlarmSchedulerHelper.scheduleMedicationAlarm(context, med)
                        Log.d(TAG, "Rescheduled medication ${med.name} for tomorrow")

                        // Insert primary pending log item for tracking
                        val todayCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, med.hour)
                            set(Calendar.MINUTE, med.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        
                        val existingLog = db.medicationDao.getLogForMedicationAt(med.id, todayCalendar.timeInMillis)
                        if (existingLog == null) {
                            val newLog = AdherenceLogEntity(
                                medicationId = med.id,
                                medName = med.name,
                                scheduledTimestamp = todayCalendar.timeInMillis,
                                status = "PENDING"
                            )
                            db.medicationDao.insertAdherenceLog(newLog)
                            Log.d(TAG, "Created a pending log entry for ${med.name} at: ${todayCalendar.timeInMillis}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in AlarmReceiver GoAsync DB processing: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
