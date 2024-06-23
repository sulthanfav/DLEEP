package com.example.dleep2.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepMonitorService : Service() {

    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock

    private var timeToBed: Long = 0
    private var lastActivityTime: Long = System.currentTimeMillis()
    private var sleepStartTime: Long = 0

    private val INACTIVITY_THRESHOLD = 5000L // 5 seconds
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var monitoringJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SleepMonitorService::WakeLock")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timeToBed = intent?.getLongExtra("TIME_TO_BED", 0) ?: 0
        lastActivityTime = System.currentTimeMillis() // Update here as well
        Log.d(TAG, "Service started. Time to bed: ${dateFormat.format(Date(timeToBed))}")
        Log.d(TAG, "Initial last activity time: ${dateFormat.format(Date(lastActivityTime))}")
        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                checkDeviceActivity()
                delay(1000) // Check every second
            }
        }
    }

    private fun checkDeviceActivity() {
        val currentTime = System.currentTimeMillis()
        val isScreenOn = powerManager.isInteractive

        Log.d(TAG, "Checking device activity at ${dateFormat.format(Date(currentTime))}")
        Log.d(TAG, "Screen is ${if (isScreenOn) "on" else "off"}")
        Log.d(TAG, "Time to bed: ${dateFormat.format(Date(timeToBed))}")
        Log.d(TAG, "Last activity time: ${dateFormat.format(Date(lastActivityTime))}")
        Log.d(TAG, "Sleep start time: ${if (sleepStartTime != 0L) dateFormat.format(Date(sleepStartTime)) else "Not set"}")

        if (currentTime >= timeToBed) {
            if (!isScreenOn) {
                // User sedang tidur
                if (currentTime - lastActivityTime > INACTIVITY_THRESHOLD && sleepStartTime == 0L) {
                    // User telah tidur (tidak ada aktivitas layar dalam waktu INACTIVITY_THRESHOLD)
                    sleepStartTime = currentTime
                    Log.d(TAG, "User fell asleep at ${dateFormat.format(Date(sleepStartTime))}")
                }
            } else {
                // User telah bangun
                if (sleepStartTime != 0L) {
                    // User bangun setelah tidur
                    val sleepEndTime = currentTime
                    val sleepDuration = sleepEndTime - sleepStartTime
                    Log.d(TAG, "User woke up. Sleep duration: ${sleepDuration / 1000} seconds")

                    // Simpan data tidur hanya jika durasi tidur selesai
                    saveSleepData(sleepStartTime, sleepEndTime, sleepDuration)
                    sleepStartTime = 0
                }
                lastActivityTime = currentTime // Update last activity time
            }
        } else {
            Log.d(TAG, "Not yet bedtime")
        }
    }

    private fun saveSleepData(startTime: Long, endTime: Long, duration: Long) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { firebaseUser ->
            val db = FirebaseFirestore.getInstance()
            val currentDate = dateOnlyFormat.format(Date(startTime))
            val sleepData = hashMapOf(
                "userId" to firebaseUser.uid,
                "sleepStartTime" to startTime,
                "sleepEndTime" to endTime,
                "sleepDuration" to duration,
                "date" to currentDate
            )

            db.collection("SleepData")
                .add(sleepData)
                .addOnSuccessListener {
                    Log.d(TAG, "Sleep data saved successfully with ID: ${it.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error saving sleep data", e)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        monitoringJob?.cancel()
    }

    companion object {
        private const val TAG = "SleepMonitorService"
    }
}
