package com.example.dleep2.disc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dleep2.R
import com.example.dleep2.SeeMoreFragment
import com.example.dleep2.adapters.AsmrAdapter
import com.example.dleep2.adapters.RecentlyPlayedAdapter
import com.example.dleep2.adapters.TcoybAdapter
import com.example.dleep2.data.entities.Song
import com.example.dleep2.databinding.FragmentDiscoverSoundsBinding
import com.example.dleep2.other.Status
import com.example.dleep2.ui.viewmodels.MainViewModel
import com.example.dleep2.ui.viewmodels.RecentlyPlayedViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverSoundsFragment : Fragment() {
    private var _binding: FragmentDiscoverSoundsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var AsmrAdapter: AsmrAdapter
    @Inject
    lateinit var TcoybAdapter: TcoybAdapter
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

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        recentlyPlayedViewModel.recentlyPlayed.observe(viewLifecycleOwner) { recentlyPlayed ->
            recentlyPlayedAdapter.updateData(recentlyPlayed)
        }
        setupRecyclerView()
        setuprecyclerViewASMR()
        setuprecyclerViewTcoyb()
        AsmrAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
        TcoybAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
        subscribeToObservers()
        binding.textView28.setOnClickListener {
            val bundle = Bundle().apply {
                putString("filter_type", "lofi")
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
        binding.textView30.setOnClickListener {
            // Pindah ke fragment SeeMore
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, SeeMoreFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setuprecyclerViewASMR() {
        binding.recyclerViewASMR.apply {
            adapter = AsmrAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

    }
    private fun setuprecyclerViewTcoyb() {
        binding.recyclerViewTCOYB.apply {
            adapter = TcoybAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        // Filter lagu yang memiliki type "lofi"
                        val lofiSongs = songs.filter { it.type == "lofi" }
                        AsmrAdapter.songs = lofiSongs
                        TcoybAdapter.songs = songs
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
