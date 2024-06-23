package com.example.dleep2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.example.dleep2.R
import com.example.dleep2.data.entities.Song
import com.example.dleep2.databinding.ItemRecentlyMusicBinding
import javax.inject.Inject

class CRAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.item_recently_music) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: BaseSongAdapter.SongViewHolder, position: Int) {
        val song = songs[position]
        if (holder is SongViewHolder) {
            holder.bind(song)
        }
    }

    inner class SongViewHolder(private val binding: ItemRecentlyMusicBinding) : BaseSongAdapter.SongViewHolder(binding.root) {
        val judul = binding.MusicTitle
        val pic = binding.IconMusic

        fun bind(song: Song) {
            judul.text = song.title
            glide.load(song.imageUrl).into(pic)

            itemView.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSongAdapter.SongViewHolder {
        val binding = ItemRecentlyMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }
}
