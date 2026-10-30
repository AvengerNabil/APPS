package com.nmplayer.video.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nmplayer.video.R
import com.nmplayer.video.data.PreferencesManager
import com.nmplayer.video.databinding.FragmentFolderBinding
import com.nmplayer.video.model.Folder
import com.nmplayer.video.ui.adapter.FolderAdapter
import com.nmplayer.video.ui.adapter.FolderListener
import com.nmplayer.video.util.FolderUtil

class FolderFragment : Fragment(R.layout.fragment_folder), FolderListener {

    private lateinit var binding: FragmentFolderBinding
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFolderBinding.inflate(inflater, container, false)
        prefsManager = PreferencesManager(requireContext())
        folderAdapter = FolderAdapter(this)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = folderAdapter
        }

        checkPermission()

        binding.btnResumeVideo.setOnClickListener {
            resumeLastVideo()
        }
        return binding.root
    }

    private val permissionsResultCallback = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var granted = false
        permissions.entries.forEach {
            if (it.value) granted = true
        }
        if (granted) {
            loadFolders()
        } else {
            Toast.makeText(requireContext(), "Storage permission is required to list videos", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadFolders() {
        val list = FolderUtil.getAllFolder(requireContext())
        folderAdapter.submitList(list)
    }

    override fun onFolderClicked(position: Int, folder: Folder) {
        val action = FolderFragmentDirections.actionFolderFragmentToVideoFragment(
            folder.id,
            folder.folderName
        )
        findNavController().navigate(action)
    }

    private fun checkPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            permissionsResultCallback.launch(permissions)
        } else {
            loadFolders()
        }
    }

    private fun resumeLastVideo() {
        val videoId = prefsManager.lastVideoId
        val folderId = prefsManager.lastFolderId

        if (videoId == -1L) {
            Toast.makeText(requireContext(), getString(R.string.no_recently_video_found), Toast.LENGTH_SHORT).show()
            return
        }

        val video = FolderUtil.findVideo(requireContext(), folderId, videoId)
        if (video != null) {
            val action = FolderFragmentDirections.actionFolderFragmentToPlayerFragment(
                folderId,
                -1,
                video
            )
            findNavController().navigate(action)
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_recently_video_found), Toast.LENGTH_SHORT).show()
        }
    }
}

