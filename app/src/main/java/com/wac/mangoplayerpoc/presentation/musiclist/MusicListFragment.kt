package com.wac.mangoplayerpoc.presentation.musiclist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.wac.mangoplayerpoc.R
import com.wac.mangoplayerpoc.databinding.ActivityMainBinding
import com.wac.mangoplayerpoc.databinding.FragmentMusicListBinding
import com.wac.mangoplayerpoc.presentation.adapter.SongListAdapter
import com.wac.mangoplayerpoc.presentation.main.MainActivity
import com.wac.mangoplayerpoc.presentation.main.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import javax.inject.Inject

@AndroidEntryPoint
class MusicListFragment : Fragment(R.layout.fragment_music_list) {
    private lateinit var binding: FragmentMusicListBinding

    @Inject
    lateinit var glide: RequestManager

    private lateinit var musicListViewModel: MusicListViewModel

    private val activityViewModel: MainActivityViewModel by activityViewModels()

    private val songListAdapter: SongListAdapter by lazy { SongListAdapter(glide) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicListViewModel = ViewModelProvider(requireActivity())[MusicListViewModel::class.java]
        binding = FragmentMusicListBinding.bind(view)


        setupRecyclerView()
        subscribeToObserver()
        setInsetter()
        songListAdapter.setOnItemClickListener {
            musicListViewModel.setSong()
            activityViewModel.setVideoPlay(false)
//            activityViewModel.playOrToggleSong(it,false)
            findNavController().navigate(
                MusicListFragmentDirections.mobNavigationToNowPlayingFragment(
                    it
                )
            )
        }
    }

    fun navigateToSong() {
        findNavController().navigate(
            MusicListFragmentDirections.mobNavigationToNowPlayingFragment(
                null
            )
        )
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

    private fun subscribeToObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                musicListViewModel.mediaItem.collect { result ->
                    result.song?.let {
                        songListAdapter.submitList(it)
                    }
                    loading(result.loading)
                }
            }
        }

    }

    private fun loading(load: Boolean) {
        with(binding) {
            progressBar.isVisible = load
            recyclerView.isVisible = !load
        }
    }
}