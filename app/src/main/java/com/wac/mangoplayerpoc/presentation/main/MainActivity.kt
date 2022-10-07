package com.wac.mangoplayerpoc.presentation.main

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.animation.TranslateAnimation
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.RequestManager
import com.wac.mangoplayerpoc.MobNavigationDirections
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    lateinit var destination: NavDestination
    lateinit var playingSong: Song
    private var shouldUpdateSeekbar = true

    private val isPipSupported by lazy {
        packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

    @Inject
    lateinit var glide: RequestManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        observeMusic()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(binding.root)
        setUpNavigation()
        setInsetter()
        setClickListener()

    }


    private fun setClickListener() {
        binding.curPlayingItem.setOnClickListener {
            val extras =
                FragmentNavigatorExtras(binding.curPlayingItem to "shared_element_container")
            navController.navigate(
                MobNavigationDirections.mobNavigationToNowPlayingFragment(
                    playingSong
                ), extras
            )
        }
        binding.includeCurPlaying.playPause.setOnClickListener {
            mainViewModel.playOrToggleSong(playingSong, true)
        }
        binding.includeCurPlaying.nextSong.setOnClickListener {
            mainViewModel.skipToNextSong()
        }

        binding.linearProgressIndicator.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seek: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seek: SeekBar?) {
                seek?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }

        })
    }

    private fun getPipRatio(): Rational {
        val width = window.decorView.measuredWidth
        val height = window.decorView.measuredHeight
        return Rational(16, 9)
    }

    private fun updatedPipParams(): PictureInPictureParams? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PictureInPictureParams.Builder()
                    .setSeamlessResizeEnabled(true)
                    .setAspectRatio(getPipRatio())
                    .build()
            } else {
                null
            }
        } else {
            null
        }
    }


    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isPipSupported) {
            return
        }
        if (mainViewModel.isPlayingSong && destination.id == R.id.nowPlayingFragment && mainViewModel.showVideo) {
            updatedPipParams()?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enterPictureInPictureMode(it)
                }
            }
        }
    }


    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        val show = isInPictureInPictureMode && mainViewModel.isPlayingSong
        mainViewModel.setPipMode(show)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        }
    }


    private fun setCurPlayingData(song: Song) {
        with(binding.includeCurPlaying) {
            glide.load(song.imageUrl).into(songIcon)
            songTitle.text = song.title
        }

    }

    private fun setInsetter() {
        with(binding) {
            bottomNavigationView.applyInsetter {
                type(navigationBars = true) {
                    padding()
                }
            }
        }
    }


    private fun setUpNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.musicListFragment,
                R.id.nowPlayingFragment,

                )
        )
        navController.addOnDestinationChangedListener { _, navDestination, _ ->
            destination = navDestination
            observeMusic()
            binding.bottomNavigationView.isVisible = destination.id != R.id.nowPlayingFragment
            if (destination.id == R.id.videoMusicListFragment) {
                mainViewModel.setVideoPlay(true)
            }
            if (destination.id == R.id.musicListFragment) {
                mainViewModel.setVideoPlay(false)
            }

            handleBottomNavigationVisibility(destination.id)

        }

        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun handleBottomNavigationVisibility(destination: Int) {
        if (destination == R.id.nowPlayingFragment) {
//            val animate =
//                TranslateAnimation(0f, 0f, binding.bottomNavigationView.height.toFloat(), 0f)
//            animate.duration = 400
//            animate.fillAfter = true
//            binding.bottomNavigationView.startAnimation(animate)
//
//        }
//        else {
            val animate =
                TranslateAnimation(0f, 0f, 0f, binding.bottomNavigationView.height.toFloat())
            animate.duration = 500
            binding.bottomNavigationView.startAnimation(animate)
        }
        else{
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            WindowCompat.setDecorFitsSystemWindows(this.window, true)
            WindowInsetsControllerCompat(this.window, binding.root).show(
                WindowInsetsCompat.Type.systemBars()
            )
        }
    }

    private fun observeMusic() {
        lifecycleScope.launchWhenStarted {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.mediaItem.collect {
                    if (it.song != null) {
                        playingSong = it.song
                        setCurPlayingData(it.song)

                        binding.curPlayingItem.isVisible =
                            destination.id != R.id.nowPlayingFragment && it.showLastPlayedSong
                        binding.linearProgressIndicator.isVisible = binding.curPlayingItem.isVisible

                        binding.linearProgressIndicator.max = it.duration.toInt()
                        binding.linearProgressIndicator.progress = it.progress
                    }
                    binding.includeCurPlaying.playPause.setImageResource(if (it.isPlaying) R.drawable.ic_round_pause_24 else R.drawable.ic_round_play_arrow_24)
                }
            }
        }
    }
}