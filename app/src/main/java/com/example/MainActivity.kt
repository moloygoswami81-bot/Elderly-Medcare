package com.example

import android.Manifest
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.MedicationRepository
import com.example.ui.screens.MedicationAppMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MedicationViewModel
import com.example.viewmodel.MedicationViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MedicationViewModel
    private val TAG = "MainActivity"

    // Alarm active broadcast receiver to catch alarms in real-time when activity is open
    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == "com.example.ALARM_TRIGGERED") {
                val id = intent.getIntExtra("MED_ID", -1)
                val name = intent.getStringExtra("MED_NAME") ?: "Medicine"
                val dosage = intent.getStringExtra("MED_DOSAGE") ?: ""
                Log.d(TAG, "Foreground event caught: Alarm ringing for ID: $id ($name)")
                
                viewModel.receiveAlarmTrigger(id, name, dosage)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Bypass Lock Screen & Force Screen On for critical emergency reminders
        configureLockScreenBypass()

        // 2. Initialize Room Database and ViewModel
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = MedicationRepository(db.medicationDao)
        val factory = MedicationViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[MedicationViewModel::class.java]

        // 3. Process incoming alert launch parameters (if launched by clicking Notification Full Screen Intent)
        handleNotificationLaunchIntent(intent)

        // 4. Request standard runtime permissions for audio verification and notifications
        requestSystemPermissions()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { pad ->
                        _xh_unused(pad) // safety check
                        MedicationAppMainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Register receiver for real-time foreground alerts when active
        val filter = IntentFilter("com.example.ALARM_TRIGGERED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(alarmReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(alarmReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(alarmReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new triggers when activity is in singleTop state
        setIntent(intent)
        handleNotificationLaunchIntent(intent)
    }

    private fun handleNotificationLaunchIntent(intent: Intent?) {
        if (intent == null) return
        val hasAlarmLaunch = intent.getBooleanExtra("LAUNCH_ALARM_PANEL", false)
        if (hasAlarmLaunch) {
            val id = intent.getIntExtra("LAUNCH_MED_ID", -1)
            val name = intent.getStringExtra("LAUNCH_MED_NAME") ?: "Medicine"
            val dosage = intent.getStringExtra("LAUNCH_MED_DOSAGE") ?: ""
            Log.d(TAG, "Activity launched from Notification alert intent: $id ($name)")
            
            viewModel.receiveAlarmTrigger(id, name, dosage)
        }
    }

    private fun configureLockScreenBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun requestSystemPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Notifications permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Speech recorder audio input permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 9005)
        }
    }

    // Safety padding compiler checkout helper
    private fun _xh_unused(p: androidx.compose.foundation.layout.PaddingValues) {
        // simple utility to suppress unread variables in compiler
        val x = p.calculateBottomPadding()
        Log.v(TAG, "System padding resolved: $x")
    }
}
