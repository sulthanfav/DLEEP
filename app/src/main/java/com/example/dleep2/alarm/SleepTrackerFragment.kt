package com.example.dleep2.alarm

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dleep2.databinding.FragmentSleepTrackerBinding
import com.db.williamchart.data.AxisType
import com.db.williamchart.ExperimentalFeature
import com.db.williamchart.view.LineChartView

class SleepTrackerFragment : Fragment() {

    private var _binding: FragmentSleepTrackerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSleepTrackerBinding.inflate(inflater, container, false)
        val view = binding.root

        // Setup chart
        setupLineChart()

        return view
    }

    private fun setupLineChart() {
        val lineChart = binding.lineChart
        lineChart.animation.duration = animationDuration
        lineChart.animate(lineSet)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val lineSet = listOf(
            "Senin" to 1f,
            "Selasa" to 2f,
            "Rabu" to 3f,
            "Kamis" to 4f,
            "Jumat" to 5f,
            "Sabtu" to 6f,
            "Minggu" to 7f
        )

        private const val animationDuration = 1000L
    }
}
