package com.wac.mangoplayerpoc.presentation.videolist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.data.model.Song
import com.wac.mangoplayerpoc.databinding.FragmentVideoMisicListBinding
import com.wac.mangoplayerpoc.presentation.adapter.SongListAdapter
import com.wac.mangoplayerpoc.presentation.main.MainActivityViewModel
import com.wac.mangoplayerpoc.presentation.musiclist.MusicListFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import javax.inject.Inject

@AndroidEntryPoint
class VideoMusicListFragment : Fragment(R.layout.fragment_video_misic_list) {
    @Inject
    lateinit var glide: RequestManager

    private lateinit var binding: FragmentVideoMisicListBinding

    private val viewModel: VideoMusicListViewModel by viewModels()

    private val activityViewModel: MainActivityViewModel by activityViewModels()

    lateinit var song: MutableList<Song>
    private val songListAdapter: SongListAdapter by lazy { SongListAdapter(glide) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVideoMisicListBinding.bind(view)
        setInsetter()
        setupRecyclerView()
        val videoSong = viewModel.videoMusic.map { it.toSong() }

        songListAdapter.submitList(videoSong)


        songListAdapter.setOnItemClickListener {
            activityViewModel.setPlayVideo(true)
            viewModel.setVideoMusic()



            findNavController().navigate(
                MusicListFragmentDirections.mobNavigationToNowPlayingFragment(
                    it
                )
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
        adapter = songListAdapter
    }

}