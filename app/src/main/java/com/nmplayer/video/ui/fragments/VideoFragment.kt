package com.nmplayer.video.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nmplayer.video.R
import com.nmplayer.video.data.PreferencesManager
import com.nmplayer.video.databinding.FragmentVideoBinding
import com.nmplayer.video.firebase.FirebaseManager
import com.nmplayer.video.model.Video
import com.nmplayer.video.ui.adapter.VideoAdapter
import com.nmplayer.video.ui.adapter.VideoListener
import com.nmplayer.video.ui.viewmodel.VideoViewModel
import com.nmplayer.video.util.VideoUtil
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class VideoFragment : Fragment(R.layout.fragment_video), VideoListener {

    private lateinit var binding: FragmentVideoBinding
    private val args: VideoFragmentArgs by navArgs()
    private val viewModel: VideoViewModel by viewModels()
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var prefsManager: PreferencesManager
    private val currentVideoList = mutableListOf<Video>()

    companion object {
        const val VIDEO_LIST_BUNDLE_KEY = "video_list_bundle_key"
        const val VIDEO_BUNDLE_KEY = "video_bundle_key"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoBinding.inflate(inflater, container, false)
        prefsManager = PreferencesManager(requireContext())
        @Suppress("deprecation")
        setHasOptionsMenu(true)
        videoAdapter = VideoAdapter(this)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            FastScrollerBuilder(this).build()
            adapter = videoAdapter
        }

        loadVideos()
        return binding.root
    }

    private fun loadVideos() {
        currentVideoList.clear()
        val list = if (args.folderId != -1L) {
            VideoUtil.getVideosByFolder(requireContext(), args.folderId)
        } else {
            VideoUtil.getAllVideos(requireContext())
        }
        val favoriteIds = prefsManager.getFavoriteIds()
        list.forEach { it.isFavorite = favoriteIds.contains(it.id.toString()) }
        currentVideoList.addAll(list)
        videoAdapter.setData(currentVideoList)
        setVideoListResult(currentVideoList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as? SearchView
        searchView?.queryHint = getString(R.string.search_video)

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    videoAdapter.setData(currentVideoList)
                } else {
                    videoAdapter.filter.filter(newText)
                }
                return true
            }
        })
        @Suppress("deprecation")
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setVideoListResult(videoList: List<Video>) {
        setFragmentResult(
            VIDEO_BUNDLE_KEY,
            bundleOf(
                VIDEO_LIST_BUNDLE_KEY to ArrayList(videoList),
            )
        )
    }

    override fun onItemClicked(position: Int, video: Video) {
        val action = VideoFragmentDirections.actionVideoFragmentToPlayerFragment(
            args.folderId,
            position,
            video
        )
        findNavController().navigate(action)
    }

    override fun onItemDeleteClicked(position: Int) {
        viewModel.deleteDialog(requireContext(), position, currentVideoList, videoAdapter)
    }

    override fun showDetailsClicked(video: Video) {
        viewModel.showDetailsDialog(requireContext(), video)
    }

    override fun shareVideoClicked(video: Video) {
        VideoUtil.shareVideo(requireContext(), video)
    }

    override fun onFavoriteClicked(position: Int, video: Video) {
        val isFav = prefsManager.toggleFavorite(video.id)
        FirebaseManager.syncFavorite(video.id, isFav)
        Toast.makeText(
            requireContext(),
            if (isFav) getString(R.string.added_to_favorites) else getString(R.string.removed_from_favorites),
            Toast.LENGTH_SHORT
        ).show()
    }
}

