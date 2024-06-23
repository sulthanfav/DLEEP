package com.example.dleep2.disc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dleep2.adapters.VideoAdapter
import com.example.dleep2.adapters.VideoLagiAdapter
import com.example.dleep2.data.remote.VideoDatabase
import com.example.dleep2.databinding.FragmentDiscoverStoriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscoverStoriesFragment : Fragment() {

    private var _binding: FragmentDiscoverStoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiscoverStoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchVideos()
    }

    private fun fetchVideos() {
        CoroutineScope(Dispatchers.IO).launch {
            val videoDatabase = VideoDatabase()
            val videos = videoDatabase.getAllVideo()
            withContext(Dispatchers.Main) {
                binding.recyclerViewStories.apply {
                    layoutManager = GridLayoutManager(context, 2)
                    adapter = VideoAdapter(videos) { video ->
                        val intent = Intent(context, VideoActivity::class.java).apply {
                            putExtra("VIDEO_ID", video.videoId)
                            putExtra("VIDEO_TITLE", video.title)
                            putExtra("VIDEO_URL", video.videoUrl)
                            putExtra("VIDEO_DESC", video.videoDesc)
                            putExtra("VIDEO_SOURCE", video.source)
                        }
                        startActivity(intent)
                    }
                }
                binding.recyclerViewStories2.apply {
                    layoutManager = GridLayoutManager(context, 2)
                    val adapter = VideoLagiAdapter(videos) { video ->
                        val intent = Intent(context, VideoActivity::class.java).apply {
                            putExtra("VIDEO_ID", video.videoId)
                            putExtra("VIDEO_TITLE", video.title)
                            putExtra("VIDEO_URL", video.videoUrl)
                            putExtra("VIDEO_DESC", video.videoDesc)
                            putExtra("VIDEO_SOURCE", video.source)
                        }
                        startActivity(intent)
                    }
                    adapter.shuffleItems() // Shuffle items initially
                    this.adapter = adapter
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
