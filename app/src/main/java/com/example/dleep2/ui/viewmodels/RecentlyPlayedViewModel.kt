package com.example.dleep2.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dleep2.data.RecentlyPlayedDao
import com.example.dleep2.data.entities.RecentlyPlayed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentlyPlayedViewModel @Inject constructor(
    private val recentlyPlayedDao: RecentlyPlayedDao
) : ViewModel() {

    private val _recentlyPlayed = MutableLiveData<List<RecentlyPlayed>>()
    val recentlyPlayed: LiveData<List<RecentlyPlayed>> = _recentlyPlayed

    init {
        fetchRecentlyPlayed()
    }

    private fun fetchRecentlyPlayed() {
        viewModelScope.launch {
            _recentlyPlayed.postValue(recentlyPlayedDao.getRecentlyPlayed())
        }
    }
}
