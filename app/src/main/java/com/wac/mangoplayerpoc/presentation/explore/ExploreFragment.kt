package com.wac.mangoplayerpoc.presentation.explore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.databinding.FragmentExploreBinding
import com.wac.mangoplayerpoc.presentation.adapter.ReelsAdapter
import com.wac.mangoplayerpoc.presentation.main.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import javax.inject.Inject


@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {
    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentExploreBinding
    private val viewModel: ExploreViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val songListAdapter: SongListAdapter by lazy { SongListAdapter(glide) }
    private val videoListAdapter: VideoListAdapter by lazy { VideoListAdapter(glide) }
    private val reelsAdapter: ReelsAdapter by lazy { ReelsAdapter(glide) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExploreBinding.bind(view)
        setupRecyclerView()
        setupReelsRecyclerView()
        setupSongRecyclerView()
        setInsetter()
        songListAdapter.setOnItemClickListener {
            viewModel.setSong(it, false)
//            findNavController().navigate(
//                MusicListFragmentDirections.mobNavigationToPlayingNowFragment(
//
//                )
//            )
//            activityViewModel.playOrToggleSong(
//                it, false
//            )
        }
        videoListAdapter.setOnItemClickListener {
            viewModel.setVideoMusic(it, false)
//            findNavController().navigate(
//                MusicListFragmentDirections.mobNavigationToNowPlayingFragment(
//                    it
//                )
//            )
//            activityViewModel.playOrToggleSong(
//                it, false
//            )
        }
        reelsAdapter.setOnItemClickListener {
//            viewModel.setVideoMusic()
            findNavController().navigate(
                ExploreFragmentDirections.actionExploreFragmentToReelsFragment(it)
            )
        }
    }

    private fun setupSongRecyclerView() = binding.songRecyclerView.apply {
        layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        adapter = songListAdapter
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.song.collect {
                    songListAdapter.submitList(it.map { song -> song.toSong() })
                }
            }
        }

    }

    private fun setInsetter() {
        with(binding) {
            root.applyInsetter {
                type(statusBars = true) {
                    margin()
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.recyclerView.apply {
        layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        adapter = videoListAdapter
        val videoSong = viewModel.videoSong1.map { it.toSong() }
        videoListAdapter.submitList(videoSong)
//        reelsAdapter.submitList(videoSong)
    }

    private fun setupReelsRecyclerView() = binding.reelsRecyclerView.apply {
        adapter = reelsAdapter
        val videoSong = viewModel.videoSong1.map { it.toSong() }
        val reels = viewModel.reels
        reelsAdapter.submitList(reels)
    }


}