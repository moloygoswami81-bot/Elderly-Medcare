package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device rebooted, restoring alarm schedules")

            val pendingResult = goAsync()
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val activeMeds = db.medicationDao.getActiveMedications()
                    
                    for (med in activeMeds) {
                        AlarmSchedulerHelper.scheduleMedicationAlarm(context, med)
                        Log.d(TAG, "Restored alarm for medication: ${med.name} (Time: ${med.hour}:${med.minute})")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring alarms on boot: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
