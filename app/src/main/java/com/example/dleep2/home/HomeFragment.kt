package com.example.dleep2.home

import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var songAdapter: SongAdapter
    @Inject lateinit var RowSoundsHomeAdapter: RowSoundsHomeAdapter
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
        RowSoundsHomeAdapter.setItemClickListener {
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
                putString("filter_type", "all")
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
            adapter = RowSoundsHomeAdapter
        }

        mainViewModel.mediaItems.observe(viewLifecycleOwner) { resource ->
            resource.data?.let { songs ->
                RowSoundsHomeAdapter.songs = songs
            }
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        val lofiSongs = songs.filter { it.type == "all" }
                        RowSoundsHomeAdapter.songs = lofiSongs
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
