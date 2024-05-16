package com.example.dleep2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dleep2.data.entities.RecentlyPlayed
import com.example.dleep2.databinding.ItemRecentlySoundBinding

class RecentlyPlayedAdapter(
    private var songs: List<RecentlyPlayed>,
    private val listener: (RecentlyPlayed) -> Unit
) : RecyclerView.Adapter<RecentlyPlayedAdapter.SongViewHolder>() {

    inner class SongViewHolder(private val binding: ItemRecentlySoundBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: RecentlyPlayed) {
            binding.apply {
                textView24.text = song.title
                root.setOnClickListener { listener(song) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemRecentlySoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size

    // Method to update the data in the adapter
    fun updateData(newSongs: List<RecentlyPlayed>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
