package com.example.dleep2.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dleep2.data.entities.Song
import com.example.dleep2.exoplayer.MusicService
import com.example.dleep2.exoplayer.MusicServiceConnection
import com.example.dleep2.exoplayer.currentPlaybackPosition
import com.example.dleep2.exoplayer.isPlaying
import com.example.dleep2.exoplayer.toSong
import com.example.dleep2.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    private val _curPlayingSong = MutableLiveData<Song?>()
    val curPlayingSong: LiveData<Song?> = _curPlayingSong

    init {
        updateCurrentPlayerPosition()
        subscribeToObservers()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if (curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos ?: 0L)
                    _curSongDuration.postValue(MusicService.curSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
    private fun subscribeToObservers() {
        viewModelScope.launch {
            playbackState.observeForever { state ->
                if (state?.isPlaying == false && curPlayerPosition.value == curSongDuration.value) {
                    musicServiceConnection.transportControls.skipToNext()
                }
            }
            musicServiceConnection.curPlayingSong.observeForever {
                _curPlayingSong.postValue(it?.toSong())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackState.removeObserver { /* Observers go here */ }
        musicServiceConnection.curPlayingSong.removeObserver { /* Observers go here */ }
    }
}