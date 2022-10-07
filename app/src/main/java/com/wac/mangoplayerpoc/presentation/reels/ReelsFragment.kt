package com.wac.mangoplayerpoc.presentation.reels

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.databinding.FragmentReelsBinding
import com.wac.mangoplayerpoc.presentation.main.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReelsFragment : Fragment(R.layout.fragment_reels) {


    @Inject
    lateinit var glide: RequestManager

    lateinit var exoPlayer: ExoPlayer

    val args: ReelsFragmentArgs by navArgs()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    lateinit var binding: FragmentReelsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activityViewModel.isPlayingSong)
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    activityViewModel.mediaItem.collect {
                        if (it.isPlaying) {
                            it.song?.let { it1 -> activityViewModel.playOrToggleSong(it1,true) }
                        }
                    }
                }
            }


        binding = FragmentReelsBinding.bind(view)
        exoPlayer = ExoPlayer.Builder(requireContext()).apply {
//                setAudioAttributes(audioAttributes,false)
            setHandleAudioBecomingNoisy(true)
        }.build()

        exoPlayer.volume = 0f
        val mediaSource =
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireContext()))
                .createMediaSource(
                    MediaItem.fromUri(
                        args.song.video
                    )
                )
        exoPlayer.setMediaSource(
            mediaSource
        )

//        exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        binding.reelsVideoView.player = exoPlayer
        glide.load(args.song.userImage).into(binding.songIcon)
        binding.title.text = args.song.comment
        binding.album.text = args.song.userName

        //        hideSystemUI()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
        exoPlayer.stop()
        exoPlayer.release()
        //        showSystemUI()

    }

    private fun hideSystemUI() {

        WindowInsetsControllerCompat(requireActivity().window, requireView()).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowInsetsControllerCompat(requireActivity().window, requireView()).show(
            WindowInsetsCompat.Type.statusBars()
        )
    }

}