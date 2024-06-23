package com.example.dleep2.disc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dleep2.adapters.AsmrAdapter
import com.example.dleep2.adapters.CRAdapter
import com.example.dleep2.databinding.FragmentDiscoverMusicBinding
import com.example.dleep2.other.Status
import com.example.dleep2.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DiscoverMusicFragment : Fragment() {
    private var _binding: FragmentDiscoverMusicBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var CRAdapter: CRAdapter
    @Inject
    lateinit var AsmrAdapter: AsmrAdapter
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiscoverMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setuprecyclerViewCR()
        setuprecyclerViewASMR()
        CRAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
        AsmrAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
        subscribeToObservers()

        binding.MusicThumbOne.setOnClickListener {
            // Assuming you have a specific song item you want to play
            val songToPlay = mainViewModel.mediaItems.value?.data?.find { it.title == "A Kid at Heart" }
            songToPlay?.let {
                mainViewModel.playOrToggleSong(it)
            }
        }
    }


    private fun setuprecyclerViewCR() {
        binding.recyclerViewClassicalRend.apply {
            adapter = CRAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

    }

    private fun setuprecyclerViewASMR() {
        binding.recyclerViewPopularSess.apply {
            adapter = AsmrAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        // Filter lagu yang memiliki type "lofi"
                        val lofiSongs = songs.filter { it.type == "lofi" }
                        CRAdapter.songs = lofiSongs
                        AsmrAdapter.songs = songs
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
