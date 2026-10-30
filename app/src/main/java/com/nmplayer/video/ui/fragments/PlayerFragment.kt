package com.nmplayer.video.ui.fragments

import android.app.Service
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.nmplayer.video.R
import com.nmplayer.video.data.PreferencesManager
import com.nmplayer.video.databinding.FragmentPlayerBinding
import com.nmplayer.video.firebase.FirebaseManager
import com.nmplayer.video.model.Video
import com.nmplayer.video.ui.viewmodel.PlayerViewModel
import com.nmplayer.video.util.AppUtil
import com.nmplayer.video.util.VideoUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PlayerFragment : Fragment(R.layout.fragment_player) {
    private lateinit var binding: FragmentPlayerBinding
    private val args: PlayerFragmentArgs by navArgs()
    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var audioManager: AudioManager
    private lateinit var prefsManager: PreferencesManager
    private val videoList = mutableListOf<Video>()
    private var position: Int = -1
    private var isFullscreenEnable = false
    private var isLocked = false
    private var brightness = -1.0f
    private var isMuted = false
    private var isRotated = false
    private var isSubtitleEnable = true
    private var aspectRatioMode = 0 // 0=FIT, 1=FILL, 2=ZOOM
    private var exoPlayer: ExoPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPlayerBinding.bind(view)
        audioManager = requireActivity().getSystemService(Service.AUDIO_SERVICE) as AudioManager
        prefsManager = PreferencesManager(requireContext())
        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        position = args.position
        if (position != -1) {
            setUpResultListener()
        } else {
            videoList.clear()
            if (args.folderId != -1L) {
                videoList.addAll(VideoUtil.getVideosByFolder(requireContext(), args.folderId))
            } else {
                videoList.addAll(VideoUtil.getAllVideos(requireContext()))
            }
            createPlayer()
        }

        addClickListeners()
        addSeekbarChangeListeners()
        setVisibilityObserver()
    }

    private fun createPlayer() {
        val video = if (position in videoList.indices) {
            videoList[position]
        } else {
            position = VideoUtil.findVideoPosition(args.video.id, videoList)
            if (position == -1 && videoList.isNotEmpty()) position = 0
            if (position in videoList.indices) videoList[position] else args.video
        }

        viewModel.trackSelector = DefaultTrackSelector(requireContext())
        exoPlayer = ExoPlayer.Builder(requireContext())
            .setTrackSelector(viewModel.trackSelector!!)
            .build()

        binding.btnBackName.text = video.name

        val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppUtil.getRealPath(Uri.parse(video.path), requireContext()) ?: video.path
        } else {
            video.path
        }

        val mediaItem = MediaItem.fromUri(path)
        exoPlayer?.setMediaItem(mediaItem)

        // Restore playback position
        val savedPos = prefsManager.getLastPosition(video.id)
        if (savedPos > 0) {
            exoPlayer?.seekTo(savedPos)
        }

        // Apply saved playback speed
        val defaultSpeed = prefsManager.defaultSpeed
        if (defaultSpeed != 1.0f) {
            exoPlayer?.playbackParameters = PlaybackParameters(defaultSpeed)
        }

        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
        binding.videoView.player = exoPlayer

        // Auto-orient based on video aspect ratio
        if (video.height > 0 && video.width > 0) {
            (activity as? AppCompatActivity)?.requestedOrientation =
                if (video.height < video.width) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
        }

        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    nextClicked()
                }
            }
        })
    }

    private fun addClickListeners() {
        binding.btnBackName.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnPrev.setOnClickListener {
            previousClicked()
        }
        binding.btnNext.setOnClickListener {
            nextClicked()
        }
        binding.exoFullscreen.setOnClickListener {
            playInFullScreen(!isFullscreenEnable)
        }
        binding.btnLock.setOnClickListener {
            setLockPlayer()
        }
        binding.btnScreenRotation.setOnClickListener {
            setRotation()
        }
        binding.btnMute.setOnClickListener {
            isMuted = viewModel.muteClicked(isMuted, audioManager)
            binding.btnMute.setImageResource(
                if (isMuted) R.drawable.ic_volume_off_24 else R.drawable.ic_volume_up_24
            )
        }
        binding.btnAudioTrack.setOnClickListener {
            exoPlayer?.let { viewModel.setAudioTrack(requireContext(), it) }
        }
        binding.exoSubtitle.setOnClickListener {
            exoPlayer?.let { isSubtitleEnable = viewModel.setSubtitle(requireContext(), it) }
        }
        binding.btnSpeed.setOnClickListener {
            exoPlayer?.let { player ->
                viewModel.showSpeedDialog(requireContext(), player) { selectedSpeed ->
                    prefsManager.defaultSpeed = selectedSpeed
                }
            }
        }
        binding.btnAspectRatio.setOnClickListener {
            cycleAspectRatio()
        }
        binding.btnPip.setOnClickListener {
            enterPictureInPictureMode()
        }
    }

    private fun cycleAspectRatio() {
        aspectRatioMode = (aspectRatioMode + 1) % 3
        when (aspectRatioMode) {
            0 -> {
                binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                Toast.makeText(requireContext(), getString(R.string.aspect_fit), Toast.LENGTH_SHORT).show()
            }
            1 -> {
                binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                Toast.makeText(requireContext(), getString(R.string.aspect_fill), Toast.LENGTH_SHORT).show()
            }
            2 -> {
                binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                Toast.makeText(requireContext(), getString(R.string.aspect_zoom), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enterPictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val format = exoPlayer?.videoFormat
                val rational = if (format != null && format.width > 0 && format.height > 0) {
                    Rational(format.width, format.height)
                } else {
                    Rational(16, 9)
                }
                val pipBuilder = PictureInPictureParams.Builder()
                    .setAspectRatio(rational)
                requireActivity().enterPictureInPictureMode(pipBuilder.build())
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "PiP mode not supported on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "PiP requires Android 8.0+", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            binding.videoView.hideController()
            binding.videoView.useController = false
            changeVisibility(View.GONE)
        } else {
            binding.videoView.useController = true
            changeVisibility(View.VISIBLE)
        }
    }

    private fun addSeekbarChangeListeners() {
        binding.seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val layout = requireActivity().window.attributes
                layout.screenBrightness = progress / 100.toFloat()
                binding.txtBrightness.text = "$progress%"
                requireActivity().window.attributes = layout
                brightness = progress / 100.toFloat()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        binding.seekBarVolume.max = 100
        binding.seekBarVolume.progress = ((currentVol.toFloat() / maxVolume) * 100).toInt()
        binding.txtVolume.text = "${binding.seekBarVolume.progress}%"

        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, newVolume: Int, fromUser: Boolean) {
                val targetVol = ((newVolume / 100.0) * maxVolume).roundToInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                binding.txtVolume.text = "$newVolume%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setLockPlayer() {
        if (!isLocked) {
            isLocked = true
            binding.videoView.hideController()
            binding.videoView.useController = false
            binding.btnLock.setImageResource(R.drawable.ic_lock)
            changeVisibility(View.INVISIBLE)
            binding.btnLock.visibility = View.VISIBLE
            binding.btnLock.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.player_overlay))
        } else {
            isLocked = false
            binding.videoView.showController()
            binding.videoView.useController = true
            binding.btnLock.setImageResource(R.drawable.ic_lock_open)
            changeVisibility(View.VISIBLE)
            binding.btnLock.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        }
    }

    private fun changeVisibility(visibility: Int) {
        binding.topBar.visibility = visibility
        binding.bottomBar.visibility = visibility
        binding.centerNavControls.visibility = visibility
        binding.seekBarBrightness.visibility = visibility
        binding.seekBarVolume.visibility = visibility
        binding.txtBrightness.visibility = visibility
        binding.txtVolume.visibility = visibility

        if (isLocked) {
            binding.btnLock.visibility = View.VISIBLE
        }
    }

    private fun previousClicked() {
        saveCurrentProgress()
        exoPlayer?.stop()
        exoPlayer?.release()
        if (position > 0) position-- else position = videoList.size - 1
        createPlayer()
        exoPlayer?.play()
    }

    private fun nextClicked() {
        saveCurrentProgress()
        exoPlayer?.stop()
        exoPlayer?.release()
        if (position < videoList.size - 1) position++ else position = 0
        createPlayer()
        exoPlayer?.play()
    }

    private fun setUpResultListener() {
        setFragmentResultListener(VideoFragment.VIDEO_BUNDLE_KEY) { _, bundle ->
            val videos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelableArrayList(VideoFragment.VIDEO_LIST_BUNDLE_KEY, Video::class.java)
            } else {
                @Suppress("deprecation")
                bundle.getParcelableArrayList(VideoFragment.VIDEO_LIST_BUNDLE_KEY)
            }.orEmpty()
            if (videos.isNotEmpty()) {
                videoList.clear()
                videoList.addAll(videos)
                createPlayer()
            }
        }
    }

    private fun setVisibilityObserver() {
        lifecycleScope.launch {
            while (isAdded) {
                delay(200)
                if (!isLocked) {
                    if (binding.videoView.isControllerFullyVisible) {
                        changeVisibility(View.VISIBLE)
                    } else {
                        changeVisibility(View.INVISIBLE)
                    }
                }
            }
        }
    }

    private fun setRotation() {
        (activity as? AppCompatActivity)?.requestedOrientation =
            if (isRotated) {
                isRotated = false
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            } else {
                isRotated = true
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
    }

    private fun playInFullScreen(isEnable: Boolean) {
        if (isEnable) {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.exoFullscreen.setImageResource(R.drawable.ic_fullscreen_exit)
        } else {
            binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            exoPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.exoFullscreen.setImageResource(R.drawable.ic_fullscreen)
        }
        isFullscreenEnable = isEnable
    }

    private fun saveCurrentProgress() {
        val player = exoPlayer ?: return
        if (position in videoList.indices) {
            val curVideo = videoList[position]
            val currentPos = player.currentPosition
            val totalDur = player.duration
            prefsManager.saveVideoProgress(curVideo.id, args.folderId, currentPos, totalDur)
            FirebaseManager.syncHistory(curVideo.id, currentPos)
        }
    }

    private fun hideSystemUI() {
        activity?.let { act ->
            val controller = WindowInsetsControllerCompat(act.window, act.window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun showSystemUI() {
        activity?.let { act ->
            val controller = WindowInsetsControllerCompat(act.window, act.window.decorView)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onPause() {
        super.onPause()
        saveCurrentProgress()
        exoPlayer?.playWhenReady = false
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        exoPlayer?.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        saveCurrentProgress()
        showSystemUI()
        (activity as? AppCompatActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }
}

