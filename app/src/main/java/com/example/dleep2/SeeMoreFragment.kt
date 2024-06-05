package com.example.dleep2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dleep2.adapters.SeeMoreAdapter
import com.example.dleep2.databinding.FragmentSeeMoreBinding
import com.example.dleep2.other.Status
import com.example.dleep2.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SeeMoreFragment : Fragment(R.layout.fragment_see_more) {

    private var _binding: FragmentSeeMoreBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var SeeMoreAdapter: SeeMoreAdapter
    lateinit var mainViewModel: MainViewModel

    private var filterType: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        filterType = arguments?.getString("filter_type")

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSeeMoreBinding.bind(view)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        setupRecyclerViewSeeMore()


        SeeMoreAdapter.setItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }
        subscribeToObservers()

        binding.backarrowprivacypolicy.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerViewSeeMore() {
        binding.recyclerViewSoundsDream.apply {
            adapter = SeeMoreAdapter
        }

        mainViewModel.mediaItems.observe(viewLifecycleOwner) { resource ->
            resource.data?.let { songs ->
                SeeMoreAdapter.songs = songs
            }
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    result.data?.let { songs ->
                        val filteredSongs = if (filterType != null) {
                            songs.filter { it.type == filterType }
                        } else {
                            songs
                        }
                        SeeMoreAdapter.songs = filteredSongs
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