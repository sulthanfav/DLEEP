package com.example.dleep2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dleep2.databinding.ActivityServiceGuideBinding

class ServiceGuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceGuideBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root) // Menggunakan binding.root daripada R.layout.activity_service_guide

        // Menerapkan listener untuk tombol backarrowserviceguide
        binding.backarrowserviceguide.setOnClickListener {
            onBackPressed() // Kembali ke halaman sebelumnya saat tombol diklik
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.profPage) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
