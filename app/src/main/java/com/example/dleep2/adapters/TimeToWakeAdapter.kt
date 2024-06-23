package com.example.dleep2.adapters

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dleep2.R
import com.example.dleep2.alarm.AlarmReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

data class AlarmItem(var time: String, val userId: String, var isActive: Boolean, var documentId: String = "")

class TimeToWakeAdapter(
    private val context: Context,
    private val alarmItems: MutableList<AlarmItem>,
    private val alarmManager: AlarmManager,
    private val onItemClick: (AlarmItem) -> Unit
) : RecyclerView.Adapter<TimeToWakeAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmTime: TextView = itemView.findViewById(R.id.alarmtime)
        val switchToggle: Switch = itemView.findViewById(R.id.switch_toggle)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(alarmItems[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timetowake, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarmItem = alarmItems[position]
        holder.alarmTime.text = alarmItem.time
        holder.switchToggle.isChecked = alarmItem.isActive
        holder.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            alarmItem.isActive = isChecked
            updateAlarmStatusInFirestore(alarmItem)
            if (isChecked) {
                setWakeAlarm(alarmItem)
            } else {
                cancelWakeAlarm(alarmItem)
            }
        }
    }

    override fun getItemCount(): Int = alarmItems.size

    fun addAlarmItem(alarmItem: AlarmItem) {
        alarmItems.add(alarmItem)
        notifyItemInserted(alarmItems.size - 1)
        saveAlarmToFirestore(alarmItem)
        if (alarmItem.isActive) {
            setWakeAlarm(alarmItem)
        }
    }

    fun updateAlarmItem(alarmItem: AlarmItem) {
        val index = alarmItems.indexOfFirst { it.documentId == alarmItem.documentId }
        if (index != -1) {
            alarmItems[index] = alarmItem
            notifyItemChanged(index)
            updateAlarmInFirestore(alarmItem)
            if (alarmItem.isActive) {
                setWakeAlarm(alarmItem)
            } else {
                cancelWakeAlarm(alarmItem)
            }
        }
    }

    fun setWakeAlarm(alarmItem: AlarmItem) {
        val timeParts = alarmItem.time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val wakeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmItem.documentId)
            putExtra("ALARM_TYPE", "WAKE_ALARM")
        }
        val requestCode = alarmItem.documentId.hashCode()
        val wakeAlarmIntent = PendingIntent.getBroadcast(context, requestCode, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                wakeAlarmIntent
            )
            Log.d("TimeToWakeAdapter", "Wake Alarm set for ${alarmItem.time}, triggering at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("TimeToWakeAdapter", "Failed to set exact wake alarm: ${e.message}")
        }
    }

    private fun cancelWakeAlarm(alarmItem: AlarmItem) {
        val wakeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarmItem.documentId)
            putExtra("ALARM_TYPE", "WAKE_ALARM")
        }
        val requestCode = alarmItem.documentId.hashCode()
        val wakeAlarmIntent = PendingIntent.getBroadcast(context, requestCode, wakeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(wakeAlarmIntent)
        Log.d("TimeToWakeAdapter", "Wake Alarm canceled for ${alarmItem.time}")
    }

    private fun saveAlarmToFirestore(alarmItem: AlarmItem) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val alarmData = hashMapOf(
                "time" to alarmItem.time,
                "userId" to alarmItem.userId,
                "isActive" to alarmItem.isActive
            )

            db.collection("TimeToWake")
                .add(alarmData)
                .addOnSuccessListener { documentReference ->
                    alarmItem.documentId = documentReference.id
                    Log.d("TimeToWakeAdapter", "Alarm added to Firestore with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("TimeToWakeAdapter", "Error adding alarm to Firestore", e)
                }
        } ?: Log.w("TimeToWakeAdapter", "User not authenticated")
    }

    private fun updateAlarmInFirestore(alarmItem: AlarmItem) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val alarmData = hashMapOf(
                "time" to alarmItem.time,
                "isActive" to alarmItem.isActive
            )

            db.collection("TimeToWake")
                .document(alarmItem.documentId)
                .update(alarmData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("TimeToWakeAdapter", "Alarm updated in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.w("TimeToWakeAdapter", "Error updating alarm in Firestore", e)
                }
        } ?: Log.w("TimeToWakeAdapter", "User not authenticated")
    }

    private fun updateAlarmStatusInFirestore(alarmItem: AlarmItem) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()

            db.collection("TimeToWake")
                .document(alarmItem.documentId)
                .update("isActive", alarmItem.isActive)
                .addOnSuccessListener {
                    Log.d("TimeToWakeAdapter", "Alarm status updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.w("TimeToWakeAdapter", "Error updating alarm status", e)
                }
        } ?: Log.w("TimeToWakeAdapter", "User not authenticated")
    }
}