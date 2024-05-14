package com.example.dleep2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.example.dleep2.R
import com.example.dleep2.data.entities.Song
import com.example.dleep2.databinding.ItemRowSoundsBinding
import javax.inject.Inject

class RowSoundsHomeAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.item_row_sounds) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: BaseSongAdapter.SongViewHolder, position: Int) {
        val song = songs[position]
        if (holder is SongViewHolder) {
            holder.bind(song)
        }
    }

    inner class SongViewHolder(private val binding: ItemRowSoundsBinding) : BaseSongAdapter.SongViewHolder(binding.root) {
        val judul = binding.textView22

        fun bind(song: Song) {
            judul.text = song.title

            itemView.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSongAdapter.SongViewHolder {
        val binding = ItemRowSoundsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }
}
