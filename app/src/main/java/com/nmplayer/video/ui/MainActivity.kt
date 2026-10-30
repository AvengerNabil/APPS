package com.nmplayer.video.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.nmplayer.video.R
import com.nmplayer.video.data.PreferencesManager
import com.nmplayer.video.databinding.ActivityMainBinding
import com.nmplayer.video.firebase.FirebaseManager
import com.nmplayer.video.model.Video
import com.nmplayer.video.util.AppConstant
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var prefsManager: PreferencesManager

    companion object {
        var externalUri: Uri? = null
        var externalVideo: Video? = null
        private const val VIDEO_MP4 = "video/mp4"
        private const val VIDEO_MKV = "video/mkv"
        private const val VIDEO_X_MATROSKA = "video/x-matroska"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme mode (Dark/Light/System)
        prefsManager = PreferencesManager(this)
        prefsManager.applyTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase offline-first architecture
        FirebaseManager.init(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        navController = navHostFragment.navController

        handleExternalIntents(intent)
    }

    private fun handleExternalIntents(intent: Intent?) {
        if (intent == null) return

        if (intent.action == Intent.ACTION_SEND) {
            val type = intent.type ?: ""
            if (type.startsWith("video/") || type == VIDEO_MP4 || type == VIDEO_MKV || type == VIDEO_X_MATROSKA) {
                @Suppress("deprecation")
                externalUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
            }
        }

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val dataUri = intent.data!!
            if (dataUri.scheme.equals(AppConstant.CONTENT, ignoreCase = true)) {
                try {
                    val cursor = contentResolver.query(
                        dataUri,
                        arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME),
                        null,
                        null,
                        null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val pathIdx = it.getColumnIndex(MediaStore.Video.Media.DATA)
                            val nameIdx = it.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                            val path = if (pathIdx != -1) it.getString(pathIdx) else dataUri.toString()
                            val name = if (nameIdx != -1) it.getString(nameIdx) else File(path ?: "").name
                            externalVideo = Video(
                                name = name ?: "External Video",
                                path = path ?: dataUri.toString(),
                                duration = 0L,
                                id = -1L,
                                size = 0.0,
                                type = intent.type ?: "video/*",
                                date_added = "",
                                resolution = "",
                                height = 0,
                                width = 0
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (dataUri.scheme.equals(AppConstant.FILE, ignoreCase = true)) {
                val file = File(dataUri.path ?: "")
                externalVideo = Video(
                    name = file.name,
                    path = file.absolutePath,
                    duration = 0L,
                    id = -1L,
                    size = file.length().toDouble(),
                    type = "video/*",
                    date_added = "",
                    resolution = "",
                    height = 0,
                    width = 0
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

