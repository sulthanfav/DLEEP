package com.example.dleep2.disc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dleep2.adapters.RecentlyPlayedAdapter
import com.example.dleep2.data.entities.Song
import com.example.dleep2.databinding.FragmentDiscoverSoundsBinding
import com.example.dleep2.ui.viewmodels.MainViewModel
import com.example.dleep2.ui.viewmodels.RecentlyPlayedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverSoundsFragment : Fragment() {
    private var _binding: FragmentDiscoverSoundsBinding? = null
    private val binding get() = _binding!!

    private val recentlyPlayedViewModel: RecentlyPlayedViewModel by viewModels()
    private lateinit var recentlyPlayedAdapter: RecentlyPlayedAdapter
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiscoverSoundsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        recentlyPlayedViewModel.recentlyPlayed.observe(viewLifecycleOwner) { recentlyPlayed ->
            recentlyPlayedAdapter.updateData(recentlyPlayed)
        }
    }

    private fun setupRecyclerView() {
        recentlyPlayedAdapter = RecentlyPlayedAdapter(emptyList()) { recentlyPlayed ->
            // Convert RecentlyPlayed to Song
            val song = Song(
                mediaId = recentlyPlayed.mediaId,
                title = recentlyPlayed.title,
            )
            mainViewModel.playOrToggleSong(song)
        }
        binding.recyclerViewRecently.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = recentlyPlayedAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
