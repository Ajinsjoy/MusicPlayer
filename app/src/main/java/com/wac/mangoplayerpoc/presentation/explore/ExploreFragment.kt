package com.wac.mangoplayerpoc.presentation.explore

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.databinding.FragmentExploreBinding
import com.wac.mangoplayerpoc.databinding.FragmentVideoMisicListBinding
import com.wac.mangoplayerpoc.presentation.adapter.ReelsAdapter
import com.wac.mangoplayerpoc.presentation.adapter.SongListAdapter
import com.wac.mangoplayerpoc.presentation.musiclist.MusicListFragmentDirections
import com.wac.mangoplayerpoc.presentation.videolist.VideoMusicListViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import javax.inject.Inject


@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {
    @Inject
    lateinit var glide: RequestManager
    private lateinit var binding: FragmentExploreBinding
    private val viewModel: ExploreViewModel by viewModels()
    private val songListAdapter: SongListAdapter by lazy { SongListAdapter(glide) }
    private val reelsAdapter: ReelsAdapter by lazy { ReelsAdapter(glide) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExploreBinding.bind(view)
        setupRecyclerView()
        setupReelsRecyclerView()
        setInsetter()
        songListAdapter.setOnItemClickListener {
            viewModel.setVideoMusic()
            findNavController().navigate(
                MusicListFragmentDirections.mobNavigationToNowPlayingFragment(
                    it
                )
            )
        }
        reelsAdapter.setOnItemClickListener {
            viewModel.setVideoMusic()
            findNavController().navigate(
              ExploreFragmentDirections.actionExploreFragmentToReelsFragment(it)
            )
        }
    }

    private fun setInsetter() {
        with(binding) {
            recyclerView.applyInsetter {
                type(statusBars = true) {
                    margin()
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.recyclerView.apply {
        layoutManager = GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        adapter = songListAdapter
        val videoSong = viewModel.videoSong1.map { it.toSong() }
        songListAdapter.submitList(videoSong)
//        reelsAdapter.submitList(videoSong)
    }

    private fun setupReelsRecyclerView() = binding.reelsRecyclerView.apply {
        adapter = reelsAdapter
        val videoSong = viewModel.videoSong1.map { it.toSong() }
        val reels=viewModel.reels
        reelsAdapter.submitList(reels)
    }


}