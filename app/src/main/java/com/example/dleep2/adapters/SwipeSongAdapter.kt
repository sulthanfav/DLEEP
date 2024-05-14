package com.example.dleep2.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import com.example.dleep2.R
import com.example.dleep2.databinding.SwipeItemBinding

class SwipeSongAdapter : BaseSongAdapter(R.layout.swipe_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val binding = SwipeItemBinding.bind(holder.itemView)

        binding.apply {
            val text = "${song.title}"
            tvPrimary.text = text

            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SwipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding.root)
    }
}
