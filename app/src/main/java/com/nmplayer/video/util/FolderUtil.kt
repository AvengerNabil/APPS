package com.nmplayer.video.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.nmplayer.video.model.Folder
import com.nmplayer.video.model.Video
import java.io.File

object FolderUtil {
    // get all folders containing videos
    fun getAllFolder(context: Context): List<Folder> {
        val folderList = mutableListOf<Folder>()
        val tempFolderList = mutableListOf<Long>()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED
        )
        val sortOrder = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + AppConstant.ORDER_ASC
        try {
            val cursor = context.contentResolver.query(
                uri, projection, null, null,
                sortOrder
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val folderID = cursor.getLong(0)
                    if (!tempFolderList.contains(folderID)) {
                        tempFolderList.add(folderID)
                        val folderName = cursor.getString(1) ?: "Unknown Folder"
                        val count = getTotalCount(context, folderID)
                        if (count > 0) {
                            folderList.add(Folder(folderID, folderName, count))
                        }
                    }
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return folderList
    }

    private fun getTotalCount(context: Context, folderId: Long): Int {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
            )
        } else {
            @Suppress("deprecation")
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
            )
        }
        var videoCount = 0
        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                MediaStore.Video.Media.BUCKET_ID + AppConstant.LIKE_TYPE,
                arrayOf(folderId.toString()),
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val tempPath = ContentUris.withAppendedId(uri, cursor.getLong(0)).toString()
                        val realPath = AppUtil.getRealPath(Uri.parse(tempPath), context)
                        realPath ?: tempPath
                    } else {
                        cursor.getString(1) ?: ""
                    }
                    val file = File(path)
                    if (file.exists() || path.startsWith("content://")) {
                        videoCount++
                    }
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return videoCount
    }

    fun findVideo(context: Context, folderId: Long, videoId: Long): Video? {
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.VideoColumns.MIME_TYPE,
                MediaStore.Video.VideoColumns.DATE_ADDED,
                MediaStore.Video.Media.RESOLUTION,
                MediaStore.Video.VideoColumns.HEIGHT,
                MediaStore.Video.VideoColumns.WIDTH,
                MediaStore.Video.Media.DISPLAY_NAME
            )
        } else {
            @Suppress("deprecation")
            arrayOf(
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
        }
        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                MediaStore.Video.Media._ID + " = ?",
                arrayOf(videoId.toString()),
                null
            )
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val path: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentUris.withAppendedId(uri, cursor.getLong(3)).toString()
                    } else {
                        cursor.getString(1) ?: ""
                    }
                    val video = Video(
                        name = cursor.getString(0) ?: "Video",
                        path = path,
                        duration = cursor.getLong(2),
                        id = cursor.getLong(3),
                        size = cursor.getDouble(4),
                        type = cursor.getString(5) ?: "video/mp4",
                        date_added = cursor.getString(6) ?: "",
                        resolution = cursor.getString(7) ?: "",
                        height = cursor.getInt(8),
                        width = cursor.getInt(9)
                    )
                    cursor.close()
                    return video
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

