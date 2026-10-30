package com.nmplayer.video.ui.viewmodel

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nmplayer.video.R
import com.nmplayer.video.util.AppConstant
import java.util.*

class PlayerViewModel : ViewModel() {
    var trackSelector: DefaultTrackSelector? = null

    private fun playClicked(exoPlayer: ExoPlayer) {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    // Playback Speed Selection Dialog (0.25x - 2.0x)
    fun showSpeedDialog(context: Context, exoPlayer: ExoPlayer, onSpeedSelected: (Float) -> Unit) {
        val speeds = arrayOf("0.25x", "0.5x", "0.75x", "1.0x (Normal)", "1.25x", "1.5x", "1.75x", "2.0x")
        val speedValues = floatArrayOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        val currentSpeed = exoPlayer.playbackParameters.speed
        var selectedIndex = 3
        for (i in speedValues.indices) {
            if (kotlin.math.abs(speedValues[i] - currentSpeed) < 0.05f) {
                selectedIndex = i
                break
            }
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.playback_speed))
            .setSingleChoiceItems(speeds, selectedIndex) { dialog, which ->
                val selected = speedValues[which]
                exoPlayer.playbackParameters = PlaybackParameters(selected)
                onSpeedSelected(selected)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun setAudioTrack(context: Context, exoPlayer: ExoPlayer) {
        val pref = context.getSharedPreferences(AppConstant.AUDIO_TRACK, Context.MODE_PRIVATE)
        var selectedTrack = pref.getInt(AppConstant.SELECTED_AUDIO_TRACK, 0)

        val audioTracks = mutableListOf<String>()
        val trackGroup = exoPlayer.currentTracks.groups
        for (i in 0 until trackGroup.size) {
            if (trackGroup[i].mediaTrackGroup.type == C.TRACK_TYPE_AUDIO) {
                val lang = trackGroup[i].mediaTrackGroup.getFormat(0).language
                audioTracks.add(lang ?: "und")
            }
        }
        if (audioTracks.isEmpty()) {
            audioTracks.add(context.getString(R.string.default_language))
            selectedTrack = 0
        }
        val tempTrack = audioTracks.map { s ->
            if (s == context.getString(R.string.default_language) || s == "und") {
                context.getString(R.string.default_language)
            } else {
                Locale(s).displayLanguage
            }
        }.toTypedArray()

        if (selectedTrack >= tempTrack.size) selectedTrack = 0

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.select_language))
            .setSingleChoiceItems(tempTrack, selectedTrack) { dialog, which ->
                Toast.makeText(
                    context,
                    context.getString(R.string.item_selected, tempTrack[which]),
                    Toast.LENGTH_SHORT
                ).show()
                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setPreferredAudioLanguage(audioTracks[which])
                    .build()
                val editor = context.getSharedPreferences(AppConstant.AUDIO_TRACK, Context.MODE_PRIVATE).edit()
                editor.putInt(AppConstant.SELECTED_AUDIO_TRACK, which)
                editor.apply()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun setSubtitle(context: Context, exoPlayer: ExoPlayer): Boolean {
        val pref = context.getSharedPreferences(AppConstant.SUBTITLE_TRACK, Context.MODE_PRIVATE)
        var selectedTrack = pref.getInt(AppConstant.SELECTED_SUBTITLE_TRACK, 0)

        val subtitleTracks = mutableListOf<String>()
        subtitleTracks.add(context.getString(R.string.off_subtitle))
        val trackGroup = exoPlayer.currentTracks.groups
        for (i in 0 until trackGroup.size) {
            if (trackGroup[i].mediaTrackGroup.type == C.TRACK_TYPE_TEXT) {
                val lang = trackGroup[i].mediaTrackGroup.getFormat(0).language
                subtitleTracks.add(lang ?: "und")
            }
        }
        val tempTrack = subtitleTracks.mapIndexed { index, s ->
            if (index == 0) {
                s
            } else if (s == "und") {
                "Track $index"
            } else {
                Locale(s).displayLanguage
            }
        }.toTypedArray()

        if (selectedTrack >= tempTrack.size) selectedTrack = 0
        var isSubtitleSelected = selectedTrack != 0

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.select_subtitle))
            .setSingleChoiceItems(tempTrack, selectedTrack) { dialog, which ->
                if (which == 0) {
                    Toast.makeText(context, context.getString(R.string.subtitle_off), Toast.LENGTH_SHORT).show()
                    trackSelector?.parameters = trackSelector?.buildUponParameters()
                        ?.setRendererDisabled(2, true) // Disabled text renderer
                        ?.build() ?: return@setSingleChoiceItems
                    isSubtitleSelected = false
                } else {
                    Toast.makeText(context, context.getString(R.string.item_selected, tempTrack[which]), Toast.LENGTH_SHORT).show()
                    trackSelector?.parameters = trackSelector?.buildUponParameters()
                        ?.setRendererDisabled(2, false)
                        ?.setPreferredTextLanguage(subtitleTracks[which])
                        ?.build() ?: return@setSingleChoiceItems
                    isSubtitleSelected = true
                }
                val editor = context.getSharedPreferences(AppConstant.SUBTITLE_TRACK, Context.MODE_PRIVATE).edit()
                editor.putInt(AppConstant.SELECTED_SUBTITLE_TRACK, which)
                editor.apply()
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        return isSubtitleSelected
    }

    fun muteClicked(isMuted: Boolean, audioManager: AudioManager): Boolean {
        return if (isMuted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND)
            }
            false
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND)
            }
            true
        }
    }
}

