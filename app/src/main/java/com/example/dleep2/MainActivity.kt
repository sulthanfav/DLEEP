package com.example.dleep2

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.dleep2.adapters.SwipeSongAdapter
import com.example.dleep2.alarm.AlarmFragment
import com.example.dleep2.data.entities.Song
import com.example.dleep2.databinding.ActivityMainBinding
import com.example.dleep2.disc.DiscoverFragment
import com.example.dleep2.exoplayer.isPlaying
import com.example.dleep2.exoplayer.toSong
import com.example.dleep2.home.HomeFragment
import com.example.dleep2.other.Status
import com.example.dleep2.profile.ProfileFragment
import com.example.dleep2.ui.viewmodels.MainViewModel
import com.example.dleep2.ui.viewmodels.SongViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val songViewModel: SongViewModel by viewModels() // Deklarasi instance SongViewModel

    private lateinit var bottomNavigationView: BottomNavigationView

    private val homeFragment = HomeFragment()
    private val discoverFragment = DiscoverFragment()
    private val alarmFragment = AlarmFragment()
    private val profileFragment = ProfileFragment()

    private var playbackState: PlaybackStateCompat? = null
    private var curPlayingSong: Song? = null

    private var shouldUpdateSeekbar = true

    private lateinit var imageView2: ImageView

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    openFragment(homeFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.disc -> {
                    openFragment(discoverFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.alarm -> {
                    openFragment(alarmFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.profile -> {
                    openFragment(profileFragment)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame, fragment)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setMusicPlayerVisibility(false)
        imageView2 = binding.imageView2

        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        if (savedInstanceState == null) {
            openFragment(homeFragment)
        }

        // Inisialisasi swipeSongAdapter
        binding.vpSong.adapter = swipeSongAdapter

        subscribeToObservers()

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        binding.imageButtonPlay.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
        //------------------------
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })

        binding.imageButtonPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.imageButtonNext.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
        //------------------------
    }


    private fun switchViewPagerToCurrentSong(song: Song) {
        val newIndex = swipeSongAdapter.songs.indexOfFirst { it.mediaId == song.mediaId }
        if (newIndex != -1) {
            binding.vpSong.currentItem = newIndex
            curPlayingSong = song
        }
    }


    private fun setMusicPlayerVisibility(visible: Boolean) {
        binding.apply {
            vpSong.visibility = if (visible) View.VISIBLE else View.GONE
            imageView2.visibility = if (visible) View.VISIBLE else View.GONE
            seekBar.visibility = if (visible) View.VISIBLE else View.GONE
            imageButtonPlay.visibility = if (visible) View.VISIBLE else View.GONE
            imageButtonPrevious.visibility = if (visible) View.VISIBLE else View.GONE
            imageButtonNext.visibility = if (visible) View.VISIBLE else View.GONE
            StartTime.visibility = if (visible) View.VISIBLE else View.GONE
            EndTime.visibility = if (visible) View.VISIBLE else View.GONE
            contplayer.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((curPlayingSong ?: songs[0]).imageUrl)
                                    .into(imageView2)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe

            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(imageView2)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }
        mainViewModel.playbackState.observe(this) { state ->
            state?.let {
                playbackState = state
                binding.imageButtonPlay.setImageResource(
                    if (state.isPlaying) R.drawable.pausebutton else R.drawable.play_button
                )
            }
        }
        mainViewModel.isConnected.observe(this) { result ->
            result?.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.root,
                        resource.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
        mainViewModel.networkError.observe(this) { result ->
            result?.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.root,
                        resource.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
        songViewModel.curPlayerPosition.observe(this) {
            it?.let { position ->
                if (shouldUpdateSeekbar) {
                    binding.seekBar.progress = position.toInt()
                    setCurPlayerTimeToTextView(position)
                }
            }
        }
        songViewModel.curSongDuration.observe(this) {
            it?.let { duration ->
                binding.seekBar.max = duration.toInt()
                val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
                binding.EndTime.text = dateFormat.format(duration)
            }
        }
        mainViewModel.curPlayingSong.observe(this) {
            setMusicPlayerVisibility(it != null)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.StartTime.text = dateFormat.format(ms)
    }
}