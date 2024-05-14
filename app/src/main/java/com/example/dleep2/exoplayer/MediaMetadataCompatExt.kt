package com.example.dleep2.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.dleep2.data.entities.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
        )
    }
}