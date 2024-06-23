package com.example.dleep2.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dleep2.R
import com.example.dleep2.SeeMoreFragment
import com.example.dleep2.adapters.RowSoundsHomeAdapter
import com.example.dleep2.adapters.SongAdapter
import com.example.dleep2.databinding.FragmentHomeBinding
import com.example.dleep2.other.Status
import com.example.dleep2.ui.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var songAdapter: SongAdapter
    @Inject lateinit var rowSoundsHomeAdapter: RowSoundsHomeAdapter
    lateinit var mainViewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupRecyclerView()
        setuprecyclerViewSounds()

        songAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
        rowSoundsHomeAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
        subscribeToObservers()

        binding.seemore1.setOnClickListener {
            // Pindah ke fragment SeeMore
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, SeeMoreFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.seemore2.setOnClickListener {
            val bundle = Bundle().apply {
                putString("filter_type", "soundsdream")
            }
            val seeMoreFragment = SeeMoreFragment().apply {
                arguments = bundle
            }
            // Pindah ke fragment SeeMore
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, seeMoreFragment)
                .addToBackStack(null)
                .commit()
        }

        val db = FirebaseFirestore.getInstance()

        // Mendapatkan ID pengguna saat ini
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        // Mendapatkan data pengguna dari Firestore
        currentUserUid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        binding.hello.text = "Hello, $username."
                    } else {
                        // Jika dokumen tidak ada
                        binding.hello.text = "Hello, User."
                    }
                }
                .addOnFailureListener { exception ->
                    // Penanganan kesalahan ketika gagal mengambil data dari Firestore
                    binding.hello.text = "Hello, User."
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }

        // Add the click listener for the button
        binding.button5.setOnClickListener {
            // Assuming you have a specific song item you want to play
            val songToPlay = mainViewModel.mediaItems.value?.data?.find { it.title == "Presleep Lays" }
            songToPlay?.let {
                mainViewModel.playOrToggleSong(it)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewDaily.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        mainViewModel.mediaItems.observe(viewLifecycleOwner) { resource ->
            resource.data?.let { songs ->
                songAdapter.songs = songs
            }
        }
    }

    private fun setuprecyclerViewSounds() {
        binding.recyclerViewSounds.apply {
            adapter = rowSoundsHomeAdapter
        }

        mainViewModel.mediaItems.observe(viewLifecycleOwner) { resource ->
            resource.data?.let { songs ->
                rowSoundsHomeAdapter.songs = songs
            }
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        val lofiSongs = songs.filter { it.type == "soundsdream" }
                        rowSoundsHomeAdapter.songs = lofiSongs
                        songAdapter.songs = songs
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
