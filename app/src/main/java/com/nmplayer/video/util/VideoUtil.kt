package com.nmplayer.video.util

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.nmplayer.video.R
import com.nmplayer.video.model.Video
import java.io.File
import java.io.IOException

object VideoUtil {

    // Get all videos across the device
    fun getAllVideos(context: Context): List<Video> {
        val videoList = mutableListOf<Video>()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.RESOLUTION,
            MediaStore.Video.VideoColumns.HEIGHT,
            MediaStore.Video.VideoColumns.WIDTH,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(3)
                    val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentUris.withAppendedId(uri, id).toString()
                    } else {
                        cursor.getString(1) ?: ""
                    }
                    val title = cursor.getString(0) ?: cursor.getString(10) ?: "Video"
                    videoList.add(
                        Video(
                            name = title,
                            path = path,
                            duration = cursor.getLong(2),
                            id = id,
                            size = cursor.getDouble(4),
                            type = cursor.getString(5) ?: "video/mp4",
                            date_added = cursor.getString(6) ?: "",
                            resolution = cursor.getString(7) ?: "",
                            height = cursor.getInt(8),
                            width = cursor.getInt(9)
                        )
                    )
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return videoList
    }

    // Get videos by folderId
    fun getVideosByFolder(context: Context, folderId: Long): List<Video> {
        val videoList = mutableListOf<Video>()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.RESOLUTION,
            MediaStore.Video.VideoColumns.HEIGHT,
            MediaStore.Video.VideoColumns.WIDTH,
            MediaStore.Video.Media.DISPLAY_NAME
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                MediaStore.Video.Media.BUCKET_ID + AppConstant.LIKE_TYPE,
                arrayOf(folderId.toString()),
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(3)
                    val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentUris.withAppendedId(uri, id).toString()
                    } else {
                        cursor.getString(1) ?: ""
                    }
                    val title = cursor.getString(0) ?: cursor.getString(10) ?: "Video"
                    videoList.add(
                        Video(
                            name = title,
                            path = path,
                            duration = cursor.getLong(2),
                            id = id,
                            size = cursor.getDouble(4),
                            type = cursor.getString(5) ?: "video/mp4",
                            date_added = cursor.getString(6) ?: "",
                            resolution = cursor.getString(7) ?: "",
                            height = cursor.getInt(8),
                            width = cursor.getInt(9)
                        )
                    )
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return videoList
    }

    fun findVideoPosition(videoId: Long, videoList: List<Video>): Int {
        return videoList.indexOfFirst { it.id == videoId }
    }

    // Share Video file
    fun shareVideo(context: Context, video: Video) {
        try {
            val uriToShare: Uri = if (video.path.startsWith("content://")) {
                Uri.parse(video.path)
            } else {
                val file = File(video.path)
                FileProvider.getUriForFile(
                    context,
                    AppConstant.FILE_PROVIDER,
                    file
                )
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (video.type.isNotBlank()) video.type else AppConstant.VIDEO_MIME_TYPE
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                putExtra(Intent.EXTRA_STREAM, uriToShare)
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.sharing_file, video.name)
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Sorting functions
    fun sortByName(list: List<Video>, ascending: Boolean = true): List<Video> {
        return if (ascending) list.sortedBy { it.name.lowercase() } else list.sortedByDescending { it.name.lowercase() }
    }

    fun sortByDate(list: List<Video>, newestFirst: Boolean = true): List<Video> {
        return if (newestFirst) list.sortedByDescending { it.date_added } else list.sortedBy { it.date_added }
    }

    fun sortBySize(list: List<Video>, largestFirst: Boolean = true): List<Video> {
        return if (largestFirst) list.sortedByDescending { it.size } else list.sortedBy { it.size }
    }

    fun sortByDuration(list: List<Video>, longestFirst: Boolean = true): List<Video> {
        return if (longestFirst) list.sortedByDescending { it.duration } else list.sortedBy { it.duration }
    }
}

