package com.nmplayer.video.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nmplayer.video.databinding.VideoItemBinding
import com.nmplayer.video.model.Video
import com.nmplayer.video.ui.adapter.viewholder.VideoViewHolder
import me.zhanghai.android.fastscroll.PopupTextProvider

class VideoAdapter(private val videoListener: VideoListener) :
    ListAdapter<Video, VideoViewHolder>(VideoDiffCallback()), Filterable, PopupTextProvider {

    private val fullList = mutableListOf<Video>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding, videoListener)
    }

    override fun getPopupText(position: Int): String {
        val item = getItem(position)
        return if (item.name.isNotBlank()) item.name.substring(0, 1).uppercase() else ""
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Video, newItem: Video) = oldItem == newItem
    }

    fun setData(list: List<Video>) {
        this.fullList.clear()
        this.fullList.addAll(list)
        submitList(list)
    }

    override fun getFilter(): Filter = customFilter

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = mutableListOf<Video>()
            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(fullList)
            } else {
                val query = constraint.toString().lowercase().trim()
                for (item in fullList) {
                    if (item.name.lowercase().contains(query)) {
                        filteredList.add(item)
                    }
                }
            }
            return FilterResults().apply { values = filteredList }
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            val result = filterResults?.values as? List<Video> ?: emptyList()
            submitList(result)
        }
    }
}

interface VideoListener {
    fun onItemClicked(position: Int, video: Video)
    fun onItemDeleteClicked(position: Int)
    fun showDetailsClicked(video: Video)
    fun shareVideoClicked(video: Video)
    fun onFavoriteClicked(position: Int, video: Video)
}

