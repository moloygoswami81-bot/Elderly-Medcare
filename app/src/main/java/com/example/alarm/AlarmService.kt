package com.example.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class AlarmService : Service(), TextToSpeech.OnInitListener {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var tts: TextToSpeech? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var handler: Handler? = null
    
    private var isTtsInitialized = false
    private var medId = -1
    private var medName = "Medicine"
    private var medDosage = ""
    private var medLanguage = "EN" // default
    
    private val channelId = "MEDS_ALARM_CHANNEL_ID"
    private val notificationId = 8812

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmService", "AlarmService created")
        
        // Aquire WakeLock to ensure service keeps running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MedCare::AlarmServiceWakeLock").apply {
            acquire(30 * 60 * 1000L) // 30 mins max safety
        }

        tts = TextToSpeech(this, this)
        handler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "AlarmService starting")
        medId = intent?.getIntExtra("MED_ID", -1) ?: -1
        medName = intent?.getStringExtra("MED_NAME") ?: "Medicine"
        medDosage = intent?.getStringExtra("MED_DOSAGE") ?: ""
        
        // Fetch user profile language asynchronously
        val db = AppDatabase.getDatabase(this)
        val repository = MedicationRepository(db.medicationDao)
        
        // Use a simple IO coroutine to load language to prevent blocking
        CoroutineScope(Dispatchers.IO).launch {
            val profile = db.medicationDao.getProfile()
            medLanguage = profile?.language ?: "EN"
            Log.d("AlarmService", "Loaded preferred language: $medLanguage")
            
            // Start voice repeating loop
            handler?.post {
                handler?.postDelayed(voiceAlertRunnable, 2000)
            }
        }

        // Create Foreground notification
        createNotificationChannel()
        val notification = createNotification()
        startForeground(notificationId, notification)

        // Start playing alarm ringtone
        startAlarmAudio()

        // Start continuous vibration
        startVibrator()

        // Send standard local broadcast to notify UI that an alarm is active!
        val uiIntent = Intent("com.example.ALARM_TRIGGERED").apply {
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", medName)
            putExtra("MED_DOSAGE", medDosage)
            setPackage(packageName)
        }
        sendBroadcast(uiIntent)

        // Return START_STICKY to restart if crashed (survives process kills)
        return START_STICKY
    }

    private fun startAlarmAudio() {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(this@AlarmService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
                // Set volume lower since TTS will also speak
                setVolume(0.5f, 0.5f)
            } catch (e: Exception) {
                Log.e("AlarmService", "MediaPlayer error: ${e.message}", e)
            }
        }
    }

    private fun startVibrator() {
        val vibratorService = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        this.vibrator = vibratorService
        
        val pattern = longArrayOf(0, 1000, 500, 1000, 500) // play, pause, play
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means loop
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private val voiceAlertRunnable = object : Runnable {
        override fun run() {
            speakAlert()
            handler?.postDelayed(this, 12000) // repeat every 12 seconds
        }
    }

    private fun speakAlert() {
        if (!isTtsInitialized) return
        
        // Prepare localized messages
        val textToSpeak = when (medLanguage) {
            "BN" -> "অনুগ্রহ করে আপনার ঔষধ ${medName} নিন। ডোজ ${medDosage}।"
            "HI" -> "कृपया अपनी दवा ${medName} लें। खुराक ${medDosage} है।"
            else -> "Please take your medication, ${medName}. Dosage is ${medDosage}."
        }

        // Set TTS language based on target selection
        val locale = when (medLanguage) {
            "BN" -> Locale("bn", "IN")
            "HI" -> Locale("hi", "IN")
            else -> Locale.US
        }

        tts?.language = locale
        
        Log.d("AlarmService", "TTS speaking: $textToSpeak")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "MedAlert")
        } else {
            @Suppress("DEPRECATION")
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            Log.d("AlarmService", "TextToSpeech initialized successfully")
        } else {
            Log.e("AlarmService", "TextToSpeech init failed")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medicine Reminders"
            val descriptionText = "Triggers persistent alarms and voice announcements for medications."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                setSound(null, null) // Handled manually by MediaPlayer
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val titleText = when (medLanguage) {
            "BN" -> "ঔষধের সময়! ($medName)"
            "HI" -> "दवा का समय! ($medName)"
            else -> "Time for Medication! ($medName)"
        }
        val textValue = when (medLanguage) {
            "BN" -> "ডোজ: $medDosage - নিশ্চিত করতে স্পর্শ করুন"
            "HI" -> "खुराक: $medDosage - पुष्टि करने के लिए दबाएं"
            else -> "Dosage: $medDosage - Tap to confirm as Taken"
        }

        // Launch MainActivity as full screen intent to draw over keyguard
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("LAUNCH_ALARM_PANEL", true)
            putExtra("LAUNCH_MED_ID", medId)
            putExtra("LAUNCH_MED_NAME", medName)
            putExtra("LAUNCH_MED_DOSAGE", medDosage)
        }
        
        val pFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            9922,
            fullScreenIntent,
            pFlags
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(titleText)
            .setContentText(textValue)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true) // crucial for locked screen overlay!
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmService", "AlarmService destroying")
        
        // Stop loops
        handler?.removeCallbacks(voiceAlertRunnable)
        
        // Stop and release media player
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping MediaPlayer", e)
        }
        mediaPlayer = null
        
        // Stop and release vibrator
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping Vibrator", e)
        }
        vibrator = null

        // Stop and release TextToSpeech
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("AlarmService", "Error stopping TTS", e)
        }
        tts = null

        // Release WakeLock
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
