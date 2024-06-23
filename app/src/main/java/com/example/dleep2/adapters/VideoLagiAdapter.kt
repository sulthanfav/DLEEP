package com.example.dleep2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dleep2.data.entities.video
import com.example.dleep2.databinding.ItemRowStoriesBinding

class VideoLagiAdapter(
    private val videos: List<video>,
    private val onItemClick: (video) -> Unit
) : RecyclerView.Adapter<VideoLagiAdapter.VideoViewHolder>() {

    private var shuffledVideos: List<video> = videos // Default to original order

    fun shuffleItems() {
        shuffledVideos = videos.shuffled()
        notifyDataSetChanged()
    }

    inner class VideoViewHolder(private val binding: ItemRowStoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(video: video) {
            binding.contentTitle.text = video.title
            binding.root.setOnClickListener {
                onItemClick(video)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemRowStoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(shuffledVideos[position])
    }

    override fun getItemCount(): Int = shuffledVideos.size
}

