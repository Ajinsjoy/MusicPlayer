package com.wac.mangoplayerpoc.presentation.nowplaying


import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.material.transition.MaterialElevationScale
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.databinding.FragmentNowPlayingBinding
import com.wac.mangoplayerpoc.presentation.main.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class NowPlayingFragment : Fragment(R.layout.fragment_now_playing) {
    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var exoPlayer: ExoPlayer

    var fullscreen = false
    private lateinit var musicViewModel: NowPlayingViewModel
    lateinit var song: Song
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    lateinit var binding: FragmentNowPlayingBinding
    private val args: NowPlayingFragmentArgs by navArgs()
    private var shouldUpdateSeekbar = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNowPlayingBinding.bind(view)
        musicViewModel = ViewModelProvider(requireActivity())[NowPlayingViewModel::class.java]



        setUpData()
        clickListener()
        args.song?.let {
            activityViewModel.playOrToggleSong(
                it, false
            )
        }

        if (activityViewModel.showVideo) {
            binding.materialCardView.isVisible = false
            binding.includePlaySetting.root.isVisible = false
            binding.epVideoView.isVisible = true
        }

        observeMusic()
        exitTransition = MaterialElevationScale(/* growing= */ false)
        reenterTransition = MaterialElevationScale(/* growing= */ true)

        binding.epVideoView.player = exoPlayer
        Timber.tag("exoInitial").d("onViewCreated: %s", exoPlayer.videoScalingMode)

    }


    private fun observeMusic() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.mediaItem.collect {
                    if (it.pipMode) {
                        changeScreenOrientation()
                    }
                }
            }
        }
    }

    private fun changeScreenOrientation() {
        val orientation: Int = requireActivity().resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showSystemUI()

        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hideSystemUI()
        }

    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, requireView()).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        WindowInsetsControllerCompat(requireActivity().window, requireView()).show(
            WindowInsetsCompat.Type.systemBars()
        )
    }


    private fun clickListener() {

        binding.epVideoView.findViewById<AppCompatImageView>(R.id.viewType).setOnClickListener {
            changeScreenOrientation()
        }

        with(binding.includePlaySetting) {
            playPauseCard.setOnClickListener {
                song.let { it1 ->
                    activityViewModel.playOrToggleSong(
                        it1, true
                    )
                }
            }
            nextIcon.setOnClickListener {
                activityViewModel.skipToNextSong()
            }
            previousIcon.setOnClickListener {
                activityViewModel.skipToPreviousSong()
            }

            binding.epVideoView.findViewById<AppCompatImageView>(R.id.playPause)
                .setOnClickListener {
                    song.let { it1 ->
                        activityViewModel.playOrToggleSong(
                            it1, true
                        )
                    }
                }
            binding.epVideoView.findViewById<AppCompatImageView>(R.id.nextIcon).setOnClickListener {

                activityViewModel.skipToNextSong()
            }
            binding.epVideoView.findViewById<AppCompatImageView>(
                R.id.previousIcon

            )

                .setOnClickListener {

                    activityViewModel.skipToPreviousSong()
                }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        setCurrentPlayingTime(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seek: SeekBar?) {
                    shouldUpdateSeekbar = false
                }

                override fun onStopTrackingTouch(seek: SeekBar?) {
                    seek?.let {
                        activityViewModel.seekTo(it.progress.toLong())
                        shouldUpdateSeekbar = true
                    }
                }
            })


            binding.epVideoView.findViewById<SeekBar>(R.id.seekBar)
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seek: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            setCurrentPlayingTime(progress.toLong())
                        }
                    }

                    override fun onStartTrackingTouch(seek: SeekBar?) {
                        shouldUpdateSeekbar = false
                    }

                    override fun onStopTrackingTouch(seek: SeekBar?) {
                        seek?.let {
                            activityViewModel.seekTo(it.progress.toLong())
                            shouldUpdateSeekbar = true
                        }
                    }
                })
        }
        binding.playVideo.setOnCheckedChangeListener { _, check ->
            activityViewModel.showVideo = check
            binding.epVideoView.isVisible = check
            binding.materialCardView.isVisible = !check
            binding.includePlaySetting.root.isVisible = !check
        }

    }


    private fun setUpData() {
        with(binding) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    musicViewModel.mediaItem.collect {

                        binding.progressBar.isVisible =
                            it.buffering

                        it.song?.let { songItem ->
                            song = songItem
                            songName.text = songItem.title
                            glide.load(songItem.imageUrl).into(songImage)
                        }

                        binding.includePlaySetting.playPause.setImageResource(
                            if (it.isPlaying)
                                R.drawable.ic_round_pause_24
                            else
                                R.drawable.ic_round_play_arrow_24
                        )

                        binding.epVideoView.findViewById<AppCompatImageView>(R.id.playPause)
                            .setImageResource(
                                if (it.isPlaying)
                                    R.drawable.ic_round_pause_24
                                else
                                    R.drawable.ic_round_play_arrow_24
                            )

                        binding.includePlaySetting.seekBar.apply {
                            progress = it.progress
                            max = it.duration.toInt()
                        }
                        binding.epVideoView.findViewById<SeekBar>(R.id.seekBar).apply {
                            progress = it.progress
                            max = it.duration.toInt()
                        }

                        setCurrentPlayingTime(it.progress.toLong())
                        setPlayingDurationTime(it.duration)
                    }
                }
            }

            musicViewModel.currentPlayingPosition.observe(viewLifecycleOwner) {
                if (shouldUpdateSeekbar) {
                    includePlaySetting.seekBar.progress = it.toInt()
                    setCurrentPlayingTime(it)
                }
            }


            musicViewModel.currentDuration.observe(viewLifecycleOwner) {
                setPlayingDurationTime(it)
            }


        }
    }


    private fun setPlayingDurationTime(ms: Long) {
        val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("IST")
        binding.includePlaySetting.timeEnd.text = formatter.format(
            ms
        )
        binding.epVideoView.findViewById<TextView>(R.id.totalTime).text = formatter.format(
            ms
        )
    }

    private fun setCurrentPlayingTime(ms: Long) {
        val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("IST")
        binding.includePlaySetting.timeStart.text = formatter.format(ms)
        binding.epVideoView.findViewById<TextView>(R.id.currentTime).text = formatter.format(
            ms
        )
    }
}
