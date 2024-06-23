package com.example.dleep2.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dleep2.databinding.FragmentSleepTrackerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SleepTrackerFragment : Fragment() {

    private var _binding: FragmentSleepTrackerBinding? = null
    private val binding get() = _binding!!

    private val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepTrackerBinding.inflate(inflater, container, false)
        val view = binding.root

        setupUI()

        return view
    }

    private fun setupUI() {
        // Set today's date
        binding.todaydate.text = dateFormat.format(Date())

        // Load sleep data for today
        loadSleepDataForToday()

        // Load weekly sleep data for chart
        loadWeeklySleepData()
    }

    private fun loadSleepDataForToday() {
        userId?.let { uid ->
            val today = Calendar.getInstance()
            val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateOnlyFormat.format(today.time)

            firestore.collection("SleepData")
                .whereEqualTo("userId", uid)
                .whereEqualTo("date", currentDate)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val sleepData = documents.documents[0].data
                        val startSleep = sleepData?.get("sleepStartTime") as? Long ?: 0
                        val endSleep = sleepData?.get("sleepEndTime") as? Long ?: 0
                        val durationInMillis = sleepData?.get("sleepDuration") as? Long ?: 0

                        // Convert duration to hours, minutes, and seconds
                        val hours = durationInMillis / (1000 * 60 * 60)
                        val minutes = (durationInMillis % (1000 * 60 * 60)) / (1000 * 60)
                        val seconds = (durationInMillis % (1000 * 60)) / 1000

                        // Build duration string
                        val durationString = "${hours}h ${minutes}m ${seconds}s"

                        // Determine sleep quality based on age group
                        val sleepQuality = getSleepQuality(hours)

                        binding.sleepatvalue.text = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date(startSleep))
                        binding.wakeupatvalue.text = SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date(endSleep))
                        binding.sleepdurvalue.text = durationString

                        // Set sleep quality text based on age group
                        binding.ursleep.text = sleepQuality
                    } else {
                        // No sleep data found for today
                        binding.sleepatvalue.text = "No data"
                        binding.wakeupatvalue.text = "No data"
                        binding.sleepdurvalue.text = "No data"
                        binding.ursleep.text = "No sleep data"
                    }
                }
                .addOnFailureListener { e ->
                    // Error loading sleep data
                    binding.sleepatvalue.text = "Error"
                    binding.wakeupatvalue.text = "Error"
                    binding.sleepdurvalue.text = "Error"
                    binding.ursleep.text = "Error"
                }
        }
    }


    private fun getSleepQuality(hours: Long): String {
        return when {
            hours >= 9 -> "Your Sleep is good"
            hours >= 8 -> "Your Sleep is adequate"
            hours >= 7 -> "Your Sleep could be better"
            else -> "Consider getting more sleep"
        }
    }

    private fun loadWeeklySleepData() {
        userId?.let { uid ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            val startOfWeek = calendar.time

            val sleepDataList = ArrayList<Pair<String, Float>>()

            for (i in 0 until 7) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                firestore.collection("SleepData")
                    .whereEqualTo("userId", uid)
                    .whereEqualTo("date", currentDate)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val sleepDuration = documents.documents[0].getLong("sleepDuration") ?: 0
                            sleepDataList.add(getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)) to (sleepDuration / (60 * 60 * 1000)).toFloat())

                            if (sleepDataList.size == 7) {
                                // All data loaded, update chart
                                updateLineChart(sleepDataList)
                            }
                        } else {
                            // No sleep data for this day
                            sleepDataList.add(getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)) to 0f)

                            if (sleepDataList.size == 7) {
                                // All data loaded, update chart
                                updateLineChart(sleepDataList)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Error loading sleep data for this day
                        sleepDataList.add(getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)) to 0f)

                        if (sleepDataList.size == 7) {
                            // All data loaded, update chart
                            updateLineChart(sleepDataList)
                        }
                    }

                calendar.add(Calendar.DAY_OF_WEEK, 1)
            }
        }
    }

    private fun updateLineChart(data: List<Pair<String, Float>>) {
        val lineChart = binding.lineChart
        lineChart.animation.duration = animationDuration
        lineChart.animate(data)
    }

    private fun getDayOfWeek(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Minggu"
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val animationDuration = 1000L
    }
}
