package com.example.dleep2.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dleep2.adapters.AlarmItem
import com.example.dleep2.adapters.TimeToWakeAdapter
import com.example.dleep2.databinding.FragmentBedTimeBinding
import com.example.dleep2.services.SleepMonitorService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID

class BedTimeFragment : Fragment() {
    private var _binding: FragmentBedTimeBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmManager: AlarmManager
    private lateinit var bedAlarmIntent: PendingIntent
    private lateinit var timeToWakeAdapter: TimeToWakeAdapter
    private var alarmItems: MutableList<AlarmItem> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBedTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Intent for bed alarm
        val bedIntent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "BED_ALARM")
        }
        bedAlarmIntent = PendingIntent.getBroadcast(requireContext(), REQUEST_CODE_BED_ALARM, bedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Set up RecyclerView for TimeToWake
        timeToWakeAdapter = TimeToWakeAdapter(requireContext(), alarmItems, alarmManager) { alarmItem ->
            showUpdateWakeTimePickerDialog(alarmItem)
        }
        binding.TimeToWake.adapter = timeToWakeAdapter
        binding.TimeToWake.layoutManager = LinearLayoutManager(requireContext())

        binding.addalarmicon.setOnClickListener {
            showWakeTimePickerDialog()
        }

        binding.switchToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkExactAlarmPermissionAndSetBedAlarm()
                saveAlarmStatusToFirestore(true)
            } else {
                cancelBedAlarm()
                saveAlarmStatusToFirestore(false)
            }
        }

        binding.alarmconstlayout.setOnClickListener {
            showBedTimePickerDialog()
        }

        retrieveAlarmStatusFromFirestore()
        retrieveAlarmItemsFromFirestore()
    }

    private fun showBedTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.alarmtime.text = selectedTime
            val switchChecked = binding.switchToggle.isChecked
            saveAlarmTimeToFirestore(selectedTime, "TimeToBed", switchChecked)
            if (switchChecked) {
                saveAlarmStatusToFirestore(true)
                setBedAlarm()
            }
        }, hour, minute, true).show()
    }

    private fun showWakeTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            val alarmItem = AlarmItem(selectedTime, FirebaseAuth.getInstance().currentUser?.uid ?: "", true, UUID.randomUUID().toString())
            timeToWakeAdapter.addAlarmItem(alarmItem)
        }, hour, minute, true).show()
    }

    private fun showUpdateWakeTimePickerDialog(alarmItem: AlarmItem) {
        val timeParts = alarmItem.time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        TimePickerDialog(requireContext(), { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            alarmItem.time = selectedTime
            timeToWakeAdapter.updateAlarmItem(alarmItem)
        }, hour, minute, true).show()
    }

    private fun checkExactAlarmPermissionAndSetBedAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                setBedAlarm()
            } else {
                requestExactAlarmPermission()
            }
        } else {
            setBedAlarm()
        }
    }

    private fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM)
    }

    private fun setBedAlarm() {
        val time = binding.alarmtime.text.toString()
        val timeParts = time.split(":")
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

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                bedAlarmIntent
            )
            Toast.makeText(requireContext(), "Bed alarm set for ${calendar.time}", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Failed to set exact alarm: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        val serviceIntent = Intent(requireContext(), SleepMonitorService::class.java)
        serviceIntent.putExtra("TIME_TO_BED", calendar.timeInMillis)
        requireContext().startService(serviceIntent)
    }

    private fun cancelBedAlarm() {
        alarmManager.cancel(bedAlarmIntent)
        Toast.makeText(requireContext(), "Bed alarm canceled", Toast.LENGTH_SHORT).show()
    }

    private fun saveAlarmTimeToFirestore(time: String, collection: String, isActive: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val alarmData = hashMapOf(
                "time" to time,
                "userId" to user.uid,
                "isActive" to isActive
            )

            db.collection(collection)
                .document(user.uid)
                .set(alarmData)
                .addOnSuccessListener {
                    Log.d("Firestore", "Alarm time saved successfully to $collection")
                    if (collection == "TimeToBed" && isActive) {
                        setBedAlarm()
                    }
                    retrieveAlarmItemsFromFirestore()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error saving alarm time", e)
                }
        } ?: Log.w("Firestore", "User not authenticated")
    }

    private fun saveAlarmStatusToFirestore(isActive: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val alarmStatusData = hashMapOf("isActive" to isActive)

            db.collection("TimeToBed")
                .document(user.uid)
                .update(alarmStatusData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("Firestore", "Alarm status updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error updating alarm status", e)
                }
        } ?: Log.w("Firestore", "User not authenticated")
    }

    private fun retrieveAlarmStatusFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            db.collection("TimeToBed")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val isActive = document.getBoolean("isActive") ?: false
                        binding.switchToggle.isChecked = isActive
                        if (isActive) {
                            val time = document.getString("time") ?: "00:00"
                            binding.alarmtime.text = time
                            setBedAlarm()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error retrieving alarm status", e)
                }
        } ?: Log.w("Firestore", "User not authenticated")
    }

    private fun retrieveAlarmItemsFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            db.collection("TimeToWake")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    alarmItems.clear()
                    for (document in documents) {
                        val time = document.getString("time") ?: "00:00"
                        val isActive = document.getBoolean("isActive") ?: false
                        val alarmItem = AlarmItem(time, user.uid, isActive, document.id)
                        alarmItems.add(alarmItem)
                    }
                    timeToWakeAdapter.notifyDataSetChanged()
                    alarmItems.filter { it.isActive }.forEach { timeToWakeAdapter.setWakeAlarm(it) }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error retrieving alarm items", e)
                }
        } ?: Log.w("Firestore", "User not authenticated")
    }

    companion object {
        private const val REQUEST_CODE_SCHEDULE_EXACT_ALARM = 1001
        private const val REQUEST_CODE_BED_ALARM = 1002
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}