package com.nmplayer.video.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Video(
    val name: String,
    val path: String,
    val duration: Long,
    val id: Long,
    val size: Double,
    val type: String,
    val date_added: String,
    val resolution: String?,
    val height: Int,
    val width: Int,
    var lastPosition: Long = 0L,
    var isFavorite: Boolean = false
) : Parcelable

